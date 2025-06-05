package com.gandalp.gandalp.openAi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.entity.Type;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import com.gandalp.gandalp.schedule.domain.entity.Category;
import com.gandalp.gandalp.schedule.domain.entity.Schedule;
import com.gandalp.gandalp.schedule.domain.entity.ScheduleTemp;
import com.gandalp.gandalp.schedule.domain.entity.TempCategory;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleRepository;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OpenAIService {

    private final OpenAIProperties openAIProperties;
    private final ObjectMapper objectMapper;
    private final NurseRepository nurseRepository;
    private final ScheduleTempRepository scheduleTempRepository;
    private final ScheduleRepository scheduleRepository;

    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final AuthService authService;

    public ScheduleResult requestScheduleFromGPT(OpenAIRequestDTO dto) {
        Map<String, Map<String, String>> finalSchedule = new TreeMap<>();
        StringBuilder failureReasons = new StringBuilder();
        boolean overallSuccess = true;

        YearMonth yearMonth = YearMonth.of(dto.getYear(), dto.getMonth());
        int totalWeeks = (int) Math.ceil(yearMonth.lengthOfMonth() / 7.0);

        for (int week = 1; week <= totalWeeks; week++) {
            boolean weekSuccess = false;
            ScheduleResult weeklyResult = null;

            for (int attempt = 1; attempt <= 3; attempt++) {
                // 주차에 맞는 날짜 범위 계산
                LocalDate[] range = getWeekRange(dto.getYear(), dto.getMonth(), week);
                String prompt = buildPromptForWeek(dto, range[0], range[1]);
                String response = askQuestion(prompt);

                try {
                    int startIdx = response.indexOf("{");
                    int endIdx = response.lastIndexOf("}") + 1;
                    if (startIdx == -1 || endIdx == -1) {
                        weeklyResult = new ScheduleResult(false, null, "응답에 JSON이 포함되어 있지 않습니다.");
                        continue;
                    }
                    String json = response.substring(startIdx, endIdx);
                    Map<String, Map<String, String>> weekSchedule = objectMapper.readValue(json, new TypeReference<>() {});

                    weeklyResult = validateOffDayViolations(dto, weekSchedule);
                    if (weeklyResult.isSuccess()) {
                        finalSchedule.putAll(weekSchedule);
                        weekSuccess = true;
                        break;
                    }

                } catch (Exception e) {
                    weeklyResult = new ScheduleResult(false, null, "예외 발생: " + e.getMessage());
                }
            }

            if (!weekSuccess && weeklyResult != null) {
                overallSuccess = false;
                failureReasons.append("\n[Week ").append(week).append("] ").append(weeklyResult.getFailureReason());
            }
        }

        return new ScheduleResult(overallSuccess, finalSchedule,
                overallSuccess ? null : failureReasons.toString().trim());
    }

    public String askQuestion(String question) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + openAIProperties.getApiKey());

            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", question);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "gpt-4o"); //mini 고려
            ArrayNode messages = objectMapper.createArrayNode();
            messages.add(message);
            requestBody.set("messages", messages);

            StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
            httpPost.setEntity(entity);

            CloseableHttpResponse response = client.execute(httpPost);
            JsonNode root = objectMapper.readTree(response.getEntity().getContent());

            System.out.println("📦 사용된 모델: " + root.get("model").asText());

            return root.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "오류 발생: " + e.getMessage();
        }
    }
    public String buildPromptForWeek(OpenAIRequestDTO dto, LocalDate start, LocalDate end) {
        int days = (int) ChronoUnit.DAYS.between(start, end) + 1;
        int totalShifts = days * 3;

        StringBuilder prompt = new StringBuilder();

        prompt.append("너는 병원 간호사 스케줄러야. 아래 조건을 절대 어기지 마.\n")
                .append("출력은 반드시 정확한 JSON 형식으로 하고, 조건은 무조건 지켜야 해.\n\n")
                .append("📋 반드시 지켜야 할 조건:\n")
                .append("1. 근무는 DAY(06:00~14:00), EVENING(14:00~22:00), NIGHT(22:00~06:00 익일)로 총 3교대다.\n")
                .append("2. 같은 간호사가 연속으로 5일 이상 근무하면 안 된다.\n")
                .append("3. ❌ 각 간호사의 offDays 날짜에는 절대로 근무 배정하지 마. 단 하루라도 위반하면 전체 스케줄은 무효다.\n")
                .append("4. 이 주간은 ").append(days).append("일이고, 1일 3교대이므로 총 ").append(totalShifts).append("개의 근무가 존재한다.\n")
                .append("   이를 ").append(dto.getNurses().size()).append("명의 간호사가 최대한 공평하게 나누어야 한다.\n")
                .append("5. 간호사 TYPE을 참고하여 TYPE이 NURSE인 경우 절대로 NIGHT 근무에 배정하지 않아야 한다.\n")
                .append("6. 간호사 TYPE을 참고하여 TYPE이 NIGHT_NURSE인 경우 무조건 NIGHT 근무에만 배정되어야 한다.\n")
                .append("7. 간호사 TYPE이 NURSE인 경우 그 간호사의 DAY근무 수와 EVENING 근무 수의 편차가 5이하로 배정되어야 한다.\n")
                .append("8. TYPE이 NIGHT_NURSE인 간호사는 주 최대 3회까지만 근무할 수 있고, NIGHT를 3회 근무한 간호사는 그 주 근무에서 제외된다.\n")
                .append("9. 누적 근무 통계를 참고하여 간호사 타입별로 근무 수가 적은 간호사는 더 많이, 많은 간호사는 덜 배정하라.\n")
                .append("10. 모든 출력은 날짜별로 day/evening/night 키를 포함한 JSON으로 하며, 반드시 쌍따옴표를 사용하고 주석을 포함하지 마.\n\n")
                .append("📅 이번 주 스케줄 범위: ").append(start).append(" ~ ").append(end).append("\n\n")
                .append("📊 간호사 TYPE, 간호사 별 누적 근무 통계 및 오프 일정 (❌ offDays 날짜에는 절대 배정하지 마):\n");


        for (OpenAIRequestDTO.NurseScheduleInput nurse : dto.getNurses()) {
            prompt.append("- UUID: ").append(nurse.getNo())
                    .append("  TYPE=").append(nurse.getType())
                    .append(", DAY=").append(nurse.getDayWorked())
                    .append(", EVENING=").append(nurse.getEveningWorked())
                    .append(", NIGHT=").append(nurse.getNightWorked())
                    .append(", TOTAL=").append(nurse.getTotalWorked())
                    .append("  OFF_DAYS=").append(nurse.getOffDays()).append("\n");
        }

        prompt.append("\n📦 출력 예시 형식:\n")
                .append("{\n")
                .append("  \"").append(start).append("\": {\n")
                .append("    \"day\": \"간호사UUID1\",\n")
                .append("    \"evening\": \"간호사UUID2\",\n")
                .append("    \"night\": \"간호사UUID3\"\n")
                .append("  },\n")
                .append("  ...\n")
                .append("}\n");

        return prompt.toString();
    }

    public ScheduleResult validateOffDayViolations(OpenAIRequestDTO dto, Map<String, Map<String, String>> schedule) {
        Map<String, List<LocalDate>> offDayMap = dto.getNurses().stream()
                .collect(Collectors.toMap(
                        OpenAIRequestDTO.NurseScheduleInput::getNo,
                        OpenAIRequestDTO.NurseScheduleInput::getOffDays
                ));

        List<String> violations = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> dayEntry : schedule.entrySet()) {
            String dateStr = dayEntry.getKey();
            LocalDate date = LocalDate.parse(dateStr);
            Map<String, String> shifts = dayEntry.getValue();

            for (Map.Entry<String, String> shiftEntry : shifts.entrySet()) {
                String shift = shiftEntry.getKey(); // day/evening/night
                String nurseId = shiftEntry.getValue();

                List<LocalDate> offDays = offDayMap.getOrDefault(nurseId, Collections.emptyList());
                if (offDays.contains(date)) {
                    violations.add("❌ " + date + " " + shift.toUpperCase() + ": " + nurseId + "는 오프날 근무함");
                }
            }
        }

        if (!violations.isEmpty()) {
            return new ScheduleResult(false, schedule, String.join("\n", violations));
        } else {
            return new ScheduleResult(true, schedule, null);
        }
    }

    public static LocalDate[] getWeekRange(int year, int month, int week) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        int startDay = (week - 1) * 7 + 1;
        if (startDay > lastDay.getDayOfMonth()) {
            return null; // 유효하지 않은 주차
        }

        int endDay = Math.min(startDay + 6, lastDay.getDayOfMonth());

        return new LocalDate[] {
                LocalDate.of(year, month, startDay),
                LocalDate.of(year, month, endDay)
        };
    }

    public List<FairnessDTO> buildFairnessFromDatabase(){

        Department department = authService.getLoginMember().getDepartment();

        List<Nurse> allNurses = nurseRepository.findByDepartmentAndTypeNot(department,Type.HEAD_NURSE);

        List<FairnessDTO> fairnessDTOList = allNurses.stream()
                .map(nurse -> {
                    List<Schedule> schedules = scheduleRepository.findByNurseAndCategory(nurse.getId(), Category.WORKING);

                    int dayWorked = countShiftByTimeRange(schedules, "DAY");
                    int eveningWorked = countShiftByTimeRange(schedules, "EVENING");
                    int nightWorked = countShiftByTimeRange(schedules, "NIGHT");
                    int totalWorked = dayWorked + eveningWorked + nightWorked;

                    List<ScheduleTemp> scheduleTemps = scheduleTempRepository.findByNurseAndCategory(nurse.getId(), TempCategory.WORKING_TEMP);
                    int tempDayWorked = TempWorkCountShiftByTimeRange(scheduleTemps, "DAY");
                    int tempEveningWorked = TempWorkCountShiftByTimeRange(scheduleTemps, "EVENING");
                    int tempNightWorked = TempWorkCountShiftByTimeRange(scheduleTemps, "NIGHT");
                    int tempTotalWorked = tempDayWorked + tempEveningWorked + tempNightWorked;

                   return FairnessDTO.builder()
                           .nurseId(nurse.getId())
                           .nurseName(nurse.getName())
                           .dayWorked(dayWorked)
                           .eveningWorked(eveningWorked)
                           .nightWorked(nightWorked)
                           .totalWorked(totalWorked)
                           .tempDayWorked(tempDayWorked)
                           .tempEveningWorked(tempEveningWorked)
                           .tempNightWorked(tempNightWorked)
                           .tempTotalWorked(tempTotalWorked)
                           .build();
                })
                .toList();

        return fairnessDTOList;
    }



    public OpenAIRequestDTO buildRequestDTOFromDatabase() {

//        int month = LocalDate.now().plusMonths(1).getMonthValue();
        int month = 7;
        int year = LocalDate.now().plusMonths(1).getYear(); // 연도도 같이 바뀔 수 있으니 꼭 처리!

        Department department = authService.getLoginMember().getDepartment();

        List<Nurse> allNurses = nurseRepository.findByDepartmentAndTypeNot(department,Type.HEAD_NURSE);

        List<OpenAIRequestDTO.NurseScheduleInput> nurseInputs = allNurses.stream()
                .map(nurse -> {
                    List<Schedule> schedules = scheduleRepository.findByNurseAndCategory(nurse.getId(), Category.WORKING);

                    int dayWorked = countShiftByTimeRange(schedules, "DAY");
                    int eveningWorked = countShiftByTimeRange(schedules, "EVENING");
                    int nightWorked = countShiftByTimeRange(schedules, "NIGHT");
                    int totalWorked = dayWorked + eveningWorked + nightWorked;

                    List<LocalDate> offDays = schedules.stream()
                            .filter(s -> s.getCategory() == Category.ACCEPTED_OFF)
                            .map(s -> s.getStartTime().toLocalDate())
                            .distinct()
                            .toList();

                    return OpenAIRequestDTO.NurseScheduleInput.builder()
                            .no(nurse.getNo().toString())
                            .type(nurse.getType())
                            .dayWorked(dayWorked)
                            .eveningWorked(eveningWorked)
                            .nightWorked(nightWorked)
                            .totalWorked(totalWorked)
                            .offDays(offDays)
                            .build();
                })
                .toList();

        return OpenAIRequestDTO.builder()
                .year(year)
                .month(month)
                .nurses(nurseInputs)
                .build();
    }

    private int countShiftByTimeRange(List<Schedule> schedules, String shiftType) {
        int count = 0;
        for (Schedule schedule : schedules) {
            int hour = schedule.getStartTime().getHour();

            switch (shiftType) {
                case "DAY":
                    if (hour >= 6 && hour < 14) count++;
                    break;
                case "EVENING":
                    if (hour >= 14 && hour < 22) count++;
                    break;
                case "NIGHT":
                    // 나이트는 밤 10시~익일 6시 (22:00 ~ 06:00)
                    // 시간상 0~6 사이이거나 22~23 사이
                    if (hour >= 22 || hour < 6) count++;
                    break;
            }
        }
        return count;
    }

    private int TempWorkCountShiftByTimeRange(List<ScheduleTemp> schedules, String shiftType) {
        int count = 0;
        for (ScheduleTemp schedule : schedules) {
            int hour = schedule.getStartTime().getHour();

            switch (shiftType) {
                case "DAY":
                    if (hour >= 6 && hour < 14) count++;
                    break;
                case "EVENING":
                    if (hour >= 14 && hour < 22) count++;
                    break;
                case "NIGHT":
                    // 나이트는 밤 10시~익일 6시 (22:00 ~ 06:00)
                    // 시간상 0~6 사이이거나 22~23 사이
                    if (hour >= 22 || hour < 6) count++;
                    break;
            }
        }
        return count;
    }
}
