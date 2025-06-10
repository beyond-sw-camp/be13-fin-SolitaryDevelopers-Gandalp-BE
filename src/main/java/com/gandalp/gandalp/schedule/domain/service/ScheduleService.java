package com.gandalp.gandalp.schedule.domain.service;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.common.repository.CommonCodeRepository;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.member.domain.dto.NurseResponseDto;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.entity.NurseStatistics;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import com.gandalp.gandalp.member.domain.repository.NurseStatisticsRepository;
import com.gandalp.gandalp.notice.entity.Notice;
import com.gandalp.gandalp.notice.entity.NoticeCategory;
import com.gandalp.gandalp.notice.repository.NoticeRepository;
import com.gandalp.gandalp.openAi.ScheduleResult;
import com.gandalp.gandalp.schedule.domain.dto.*;
import com.gandalp.gandalp.schedule.domain.entity.*;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleRepository;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleTempRepository;
import com.gandalp.gandalp.schedule.domain.repository.SurgeryScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleTempRepository scheduleTempRepository;
    private final NurseRepository nurseRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommonCodeRepository commonCodeRepository;
    private final SurgeryScheduleRepository surgeryScheduleRepository;
    private final NurseStatisticsRepository nurseStatisticsRepository;
    private final AuthService authService;
    private final NoticeRepository noticeRepository;

    public OffScheduleTempResponseDto createOffSchecule(OffScheduleRequestDto scheduleRequestDto) {
        Optional<Nurse> nurseOpt = nurseRepository.findByEmail(scheduleRequestDto.getEmail());
        if (nurseOpt.isEmpty()) {
            throw new IllegalArgumentException("간호사를 찾을 수 없습니다.");
        }

        Nurse nurse = nurseOpt.get();
        LocalDateTime start = scheduleRequestDto.getStartTime().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime end = scheduleRequestDto.getEndTime().truncatedTo(ChronoUnit.HOURS);

        LocalDateTime now = LocalDateTime.now();

        // 과거 일정이면 hasConflict = false
        boolean isPast = end.isBefore(now) && start.isBefore(now);

//        if (isPast){
//            throw new IllegalArgumentException("과거 일정은 등록할 수 없습니다.");
//        }

        // 현재 또는 미래 일정이면 겹치는 스케줄 검사
        boolean hasConflict =
                !scheduleTempRepository.findOverlappingTempSchedules(nurse.getId(), start, end).isEmpty()
                        || !scheduleRepository.findOverlappingSchedules(nurse.getId(), start, end).isEmpty();

        if (hasConflict) {
            throw new IllegalStateException("해당 시간대에 이미 스케줄이 존재합니다.");
        }

        // 저장
        ScheduleTemp scheduleTemp = ScheduleTemp.builder()
                .nurse(nurse)
                .category(TempCategory.WAITING_OFF)
                .content(scheduleRequestDto.getContent())
                .startTime(start)
                .endTime(end)
                .build();

        scheduleTempRepository.save(scheduleTemp);

        return OffScheduleTempResponseDto.builder()
                .offScheduleTempId(scheduleTemp.getId())
                .nurseName(nurse.getName())
                .content(scheduleTemp.getContent())
                .startTime(scheduleTemp.getStartTime())
                .endTime(scheduleTemp.getEndTime())
                .build();
    }



    public NurseResponseDto checkPassword(String password, String email) {
        Optional<Nurse> nurse = nurseRepository.findByEmail(email);
        NurseResponseDto nurseResponseDto = null;
        if (nurse.isPresent() && passwordEncoder.matches(password, nurse.get().getPassword())) {
            nurseResponseDto = NurseResponseDto.builder()
                    .id(nurse.get().getId())
                    .name(nurse.get().getName())
                    .email(nurse.get().getEmail())
                    .build();
        } else {
            throw new RuntimeException("nurse is empty or password is wrong");
        }
        return nurseResponseDto;
    }

    public ScheduleResponseDto deleteOffScheduleTemp(Long scheduleTempId, String email) {
        try {

            Optional<ScheduleTemp> scheduleTemp = scheduleTempRepository.findById(scheduleTempId);
            if(scheduleTemp.isEmpty()) throw new RuntimeException("scheduleTemp is empty");

            Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("schedule_temp_category",String.valueOf(scheduleTemp.get().getCategory()));
            if(codeLabel.isEmpty()) throw new RuntimeException("Code Label is Empty");



            ScheduleResponseDto offScheduleResponseDto = ScheduleResponseDto.builder()
                    .offScheduleId(scheduleTempId)
                    .codeLabel(codeLabel.get())
                    .nurseId(scheduleTemp.get().getNurse().getId())
                    .startTime(scheduleTemp.get().getStartTime())
                    .endTime(scheduleTemp.get().getEndTime())
                    .content(scheduleTemp.get().getContent())
                    .build();

            Optional<Nurse> nurseByEmail = nurseRepository.findByEmail(email);
            if(nurseByEmail.isPresent() && nurseByEmail.get().equals(scheduleTemp.get().getNurse())) {
                scheduleTempRepository.deleteById(scheduleTempId);
            } else {
                throw new RuntimeException("유효하지 않은 이메일 입니다.");
            }


            if(scheduleTemp.get().getCategory() == TempCategory.ACCEPTED_OFF) {

                Optional<Nurse> nurse = nurseRepository.findById(scheduleTemp.get().getNurse().getId());
                if(nurse.isEmpty()) throw new RuntimeException("nurse is empty");

                Optional<Schedule> schedule = scheduleRepository.findByNurseAndStartTimeAndCategory(nurse.get(), offScheduleResponseDto.getStartTime(), Category.ACCEPTED_OFF);

                if(schedule.isPresent()) {
                    scheduleRepository.deleteById(schedule.get().getId());
                    System.out.println("삭제 성공");
                } else {
                    throw new RuntimeException("schedule is empty");
                }
            }

            return offScheduleResponseDto;

        } catch (Exception e) {

            throw new RuntimeException("승인 대기 중 오프 조회 실패");
        }
    }

    public Page<OffScheduleTempResponseDto> getOffScheduleTemp(String email, Pageable pageable) {
        try {
            Page<ScheduleTemp> scheduleTemps = scheduleTempRepository.findAllByNurseEmailContaining(email, pageable);

            // 변환
            List<OffScheduleTempResponseDto> responseDtos = scheduleTemps.stream()
                    .map(scheduleTemp -> {
                        Optional<String> codeLabelOptional = commonCodeRepository
                                .findCodeLabelByCodeGroupAndCodeValue("schedule_temp_category", String.valueOf(scheduleTemp.getCategory()));

                        return OffScheduleTempResponseDto.builder()
                                .offScheduleTempId(scheduleTemp.getId())
                                .nurseName(scheduleTemp.getNurse().getName())
                                .codeLabel(codeLabelOptional.orElse("미정")) // orElse()로 안전 처리
                                .content(scheduleTemp.getContent())
                                .startTime(scheduleTemp.getStartTime())
                                .endTime(scheduleTemp.getEndTime())
                                .updatedAt(scheduleTemp.getUpdatedAt())
                                .build();
                    })
                    .collect(Collectors.toList());

            return new PageImpl<>(responseDtos, pageable, scheduleTemps.getTotalElements());

        } catch (Exception e) {
            throw new RuntimeException("승인 대기 중 오프 조회 실패");
        }
    }


    public ScheduleResponseDto acceptOff(Long scheduleTempId) {

        Optional<ScheduleTemp> scheduleTemp = scheduleTempRepository.findById(scheduleTempId);

        if(scheduleTemp.isPresent()) {

            if(scheduleTemp.get().getCategory() == TempCategory.ACCEPTED_OFF) {
                throw new RuntimeException("이미 처리되었습니다.");
            }

            scheduleTemp.get().acceptedOff();

            Schedule schedule = Schedule.builder()
                    .nurse(scheduleTemp.get().getNurse())
                    .category(Category.ACCEPTED_OFF)
                    .content(scheduleTemp.get().getContent())
                    .startTime(scheduleTemp.get().getStartTime())
                    .endTime(scheduleTemp.get().getEndTime())
                    .build();

            scheduleRepository.save(schedule);

            System.out.println(schedule.getCategory());

            Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("SCHEDULE_CATEGORY",String.valueOf(schedule.getCategory()));

            if(codeLabel.isEmpty()) {
                throw new RuntimeException("Code Label is Empty");
            }


            recalculateCurrentMonthStatistics(schedule.getStartTime(), schedule.getNurse());


            ScheduleResponseDto offScheduleResponseDto = ScheduleResponseDto.builder()
                    .offScheduleId(schedule.getId())
                    .nurseId(schedule.getNurse().getId())
                    .codeLabel(codeLabel.get())
                    .content(schedule.getContent())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .updatedAt(scheduleTemp.get().getUpdatedAt())
                    .build();
            return offScheduleResponseDto;

        } else {
            throw new RuntimeException("scheduleTemp is empty");
        }
    }

    public ScheduleResponseDto rejectOff(Long scheduleTempId) {
        Optional<ScheduleTemp> scheduleTemp = scheduleTempRepository.findById(scheduleTempId);
        if(scheduleTemp.isEmpty()) {
            throw new RuntimeException("scheduleTemp is empty");
        } else {

            System.out.println("scheduleTemp.get().getCategory() = " + scheduleTemp.get().getCategory());

            if(scheduleTemp.get().getCategory() != TempCategory.WAITING_OFF) {

                if(scheduleTemp.get().getCategory() == TempCategory.ACCEPTED_OFF) {
                    Optional<Schedule> schedule = scheduleRepository.findByNurseAndStartTimeAndCategory(scheduleTemp.get().getNurse(), scheduleTemp.get().getStartTime(), Category.ACCEPTED_OFF);
                    if(schedule.isPresent()) {
                        scheduleRepository.deleteById(schedule.get().getId());
                    }
                } else {
                    throw new RuntimeException("이미 처리되었습니다.");
                }
            }

            scheduleTemp.get().rejectedOff();

            Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("schedule_temp_category",String.valueOf(scheduleTemp.get().getCategory()));
            if(codeLabel.isEmpty()) throw new RuntimeException("Code Label is Empty");

            ScheduleResponseDto offScheduleResponseDto = ScheduleResponseDto.builder()
                    .offScheduleId(scheduleTemp.get().getId())
                    .nurseId(scheduleTemp.get().getNurse().getId())
                    .codeLabel(codeLabel.get())
                    .content(scheduleTemp.get().getContent())
                    .startTime(scheduleTemp.get().getStartTime())
                    .endTime(scheduleTemp.get().getEndTime())
                    .updatedAt(scheduleTemp.get().getUpdatedAt())
                    .build();

            return offScheduleResponseDto;
        }
    }

    // 승인된 off CRUD

    public List<ScheduleResponseDto> getOffSchedules(String email) {

        Optional<Nurse> nurse = nurseRepository.findByEmail(email);
        if (nurse.isEmpty()) {
            throw new RuntimeException("간호사가 존재하지 않습니다.");
        } else {
            List<Schedule> schedules = scheduleRepository.findAllByNurse(nurse.get());


            List<ScheduleResponseDto> offScheduleResponseDtos = schedules.stream()
                    .map(schedule -> {
                        Optional<String> codeLabelOptional = commonCodeRepository
                                .findCodeLabelByCodeGroupAndCodeValue("schedule_category", String.valueOf(schedule.getCategory()));

                        return ScheduleResponseDto.builder()
                                .offScheduleId(schedule.getId())
                                .nurseId(schedule.getNurse().getId())
                                .content(schedule.getContent())
                                .codeLabel(codeLabelOptional.orElse("알 수 없음")) // 기본값 설정 또는 null도 가능
                                .startTime(schedule.getStartTime())
                                .endTime(schedule.getEndTime())
                                .build();
                    })
                    .toList();
            return offScheduleResponseDtos;
        }

    }

    @Transactional
    public void recalculateCurrentMonthStatistics(LocalDateTime startDateTime, Nurse nurse){



        int year = startDateTime.getYear();
        int month = startDateTime.getMonthValue();

        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.withDayOfMonth(start.toLocalDate().lengthOfMonth())
            .withHour(23).withMinute(59).withSecond(59);

        List<Schedule> schedules = scheduleRepository.findByNurseAndStartTimeBetween(nurse, start,
            end);

        int day = 0, evening = 0, night = 0, off = 0;
        for (Schedule schedule: schedules){

            if (schedule.getCategory() == Category.WORKING && schedule.getStartTime()!= null){

                Work workShift = resolveShift(schedule.getStartTime().getHour());

                switch (workShift) {
                    case DAY -> day++;
                    case EVENING -> evening++;
                    case NIGHT -> night++;
                }
            }
            else if (schedule.getCategory() == Category.ACCEPTED_OFF){
                off++;
            }
        }


        int surgeryCount = surgeryScheduleRepository.countByNurseAndMonth(nurse.getId(), start, end);

        NurseStatistics statistics = nurseStatisticsRepository
            .findByNurseIdAndYearAndMonth(nurse.getId(), year, month)
            .orElseGet(() -> NurseStatistics.builder()
                .nurse(nurse)
                .year(year)
                .month(month)
                .build());

        statistics.updateStatic(StaticsUpdateDto.builder()
            .year(year)
            .month(month)
            .dayCount(day)
            .eveningCount(evening)
            .nightCount(night)
            .offCount(off)
            .surgeryCount(surgeryCount)
            .build()
        );

        nurseStatisticsRepository.save(statistics);

    }




    private Work resolveShift(int hour) {
        if (hour >= 6 && hour < 14) return Work.DAY;
        if (hour >= 14 && hour < 22) return Work.EVENING;
        return Work.NIGHT;
    }

    public ScheduleResponseDto deleteOff(Long scheduleId) {
        try {
            Optional<Schedule> schedule = scheduleRepository.findById(scheduleId);
            Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("schedule_category",String.valueOf(schedule.get().getCategory()));
            if(codeLabel.isEmpty()) throw new RuntimeException("Code Label is Empty");

            ScheduleResponseDto offScheduleResponseDto = ScheduleResponseDto.builder()
                    .offScheduleId(scheduleId)
                    .codeLabel(codeLabel.get())
                    .nurseId(schedule.get().getNurse().getId())
                    .startTime(schedule.get().getStartTime())
                    .endTime(schedule.get().getEndTime())
                    .content(schedule.get().getContent())
                    .build();

            scheduleRepository.deleteById(scheduleId);
            return offScheduleResponseDto;
        } catch (Exception e) {
            throw new RuntimeException("오프 삭제 중 오류가 발생했습니다");
        }
    }

    public Page<OffScheduleTempResponseDto> getAllOffScheduleTemp(Pageable pageable) {

        Member loginMember = authService.getLoginMember();
        List<Nurse> nurseList = nurseRepository.findByDepartment(loginMember.getDepartment());


        Page<ScheduleTemp> all = scheduleTempRepository.findAllOffByCategory(nurseList,TempCategory.WORKING_TEMP, pageable);

        List<OffScheduleTempResponseDto> offScheduleResponseDtos = all.stream()
                .map(temp -> {
                    Optional<String> codeLabelOptional = commonCodeRepository
                            .findCodeLabelByCodeGroupAndCodeValue("schedule_temp_category", String.valueOf(temp.getCategory()));

                    return OffScheduleTempResponseDto.builder()
                            .offScheduleTempId(temp.getId())
                            .nurseName(temp.getNurse().getName())
                            .content(temp.getContent())
                            .codeLabel(codeLabelOptional.orElse("알 수 없음")) // 기본값 설정 또는 null도 가능
                            .startTime(temp.getStartTime())
                            .endTime(temp.getEndTime())
                            .updatedAt(temp.getUpdatedAt())
                            .build();
                })
                .toList();
        return new PageImpl<>(offScheduleResponseDtos, pageable, all.getTotalElements());


    }

    public Page<OffScheduleTempResponseDto> getOffScheduleTempByName(String name, Pageable pageable) {

        List<Nurse> nurse = nurseRepository.findByNameContaining(name);
        if(nurse.isEmpty()) throw new RuntimeException("간호사 조회 실패");

        Page<ScheduleTemp> scheduleTempList = scheduleTempRepository.findAllByNurseIn(nurse, pageable);

        List<OffScheduleTempResponseDto> offScheduleResponseDtos = scheduleTempList.stream()
                .map(temp -> {
                    Optional<String> codeLabelOptional = commonCodeRepository
                            .findCodeLabelByCodeGroupAndCodeValue("schedule_temp_category", String.valueOf(temp.getCategory()));

                    return OffScheduleTempResponseDto.builder()
                            .offScheduleTempId(temp.getId())
                            .nurseName(temp.getNurse().getName())
                            .content(temp.getContent())
                            .codeLabel(codeLabelOptional.orElse("알 수 없음")) // 기본값 설정 또는 null도 가능
                            .startTime(temp.getStartTime())
                            .endTime(temp.getEndTime())
                            .updatedAt(temp.getUpdatedAt())
                            .build();
                })
                .toList();
        return new PageImpl<>(offScheduleResponseDtos, pageable, scheduleTempList.getTotalElements());
    }

    public List<WorkTempResponseDto> getAllOffWorkTemp() {
        Department department = authService.getLoginMember().getDepartment();
        List<Nurse> nurseList = nurseRepository.findByDepartment(department);

        List<ScheduleTemp> scheduleTempList = scheduleTempRepository.findAllWorkByCategory(nurseList,TempCategory.WORKING_TEMP);

        List<WorkTempResponseDto> workTempResponseDtoList = scheduleTempList.stream()
                .map(work -> {
                            Optional<String> codeLabelOptional = commonCodeRepository
                                    .findCodeLabelByCodeGroupAndCodeValue("schedule_temp_category", String.valueOf(work.getCategory()));
                            if(codeLabelOptional.isEmpty()) throw new RuntimeException("Code Label is Empty");
                            return WorkTempResponseDto.builder()
                                    .workTempId(work.getId())
                                    .nurseName(work.getNurse().getName())
                                    .codeLabel(codeLabelOptional.get())
                                    .content(work.getContent())
                                    .startTime(work.getStartTime())
                                    .endTime(work.getEndTime())
                                    .build();
                        })
                .toList();

        return workTempResponseDtoList;
    }

    public WorkTempResponseDto updateWorkTemp(WorkTempRequestUpdateDto workTempRequestUpdateDto) {

        try {

            ScheduleTemp scheduleTemp = scheduleTempRepository.findById(workTempRequestUpdateDto.getWorkTempId()).orElseThrow(() -> new RuntimeException("scheduleTemp is empty"));
            Nurse nurse = nurseRepository.findById(workTempRequestUpdateDto.getNurseId()).orElseThrow(() -> new RuntimeException("nurse is empty"));
            scheduleTemp.update(nurse);

            return WorkTempResponseDto.builder()
                    .nurseName(scheduleTemp.getNurse().getName())
                    .build();

        } catch (Exception e) {

            throw new RuntimeException("scheduleTemp update error");

        }


    }

    public void deleteWorkTemp(Long workTempId) {
        try {
            scheduleRepository.deleteById(workTempId);
        } catch (Exception e) {
            throw new RuntimeException("임시 근무 삭제 실패");
        }
    }

    public List<WorkScheduleResponseDto> showWork() {

        Department department = authService.getLoginMember().getDepartment();
        List<Nurse> nurseList = nurseRepository.findByDepartment(department);

        List<Schedule> schedules = scheduleRepository.findInNurseAndCategory(nurseList, Category.WORKING);

        List<WorkScheduleResponseDto> workScheduleResponseDtos = schedules.stream()
                .map(work -> {
                    Optional<String> codeLabel = commonCodeRepository
                            .findCodeLabelByCodeGroupAndCodeValue("schedule_category", String.valueOf(work.getCategory()));
                    return WorkScheduleResponseDto.builder()
                            .workScheduleId(work.getId())
                            .nurseId(work.getNurse().getId())
                            .nurseName(work.getNurse().getName())
                            .codeLabel(codeLabel.get())
                            .content(work.getContent())
                            .startTime(work.getStartTime())
                            .endTime(work.getEndTime())
                            .build();
                })
                .toList();
        return workScheduleResponseDtos;
    }

    public ScheduleResponseDto acceptWork(Long workScheduleId) {

        Optional<ScheduleTemp> workTemp = scheduleTempRepository.findById(workScheduleId);

        if(workTemp.isEmpty()) {
            throw new RuntimeException("workTemp is empty");
        }

        scheduleTempRepository.deleteById(workTemp.get().getId());

        Schedule work = Schedule.builder()
                .nurse(workTemp.get().getNurse())
                .category(Category.WORKING)
                .content(workTemp.get().getContent())
                .startTime(workTemp.get().getStartTime())
                .endTime(workTemp.get().getEndTime())
                .build();

        try {
            scheduleRepository.save(work);

            Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("schedule_category",String.valueOf(work.getCategory()));

            if(codeLabel.isEmpty()) throw new RuntimeException("Code Label is Empty");

            ScheduleResponseDto scheduleResponseDto = ScheduleResponseDto.builder()
                    .offScheduleId(work.getId())
                    .nurseId(work.getNurse().getId())
                    .codeLabel(codeLabel.get())
                    .content(work.getContent())
                    .startTime(work.getStartTime())
                    .endTime(work.getEndTime())
                    .updatedAt(workTemp.get().getUpdatedAt())
                    .build();

            recalculateCurrentMonthStatistics(scheduleResponseDto.getStartTime(), work.getNurse());

            int month = work.getStartTime().getMonthValue();

            Notice notice = Notice.builder()
                    .category(NoticeCategory.GENERAL)
                    .content(String.format("%d월 근무표 생성", month))
                    .department(work.getNurse().getDepartment())
                    .build();

            if ( !noticeRepository.existsByContentAndCategoryAndDepartment(notice.getContent(), notice.getCategory(), notice.getDepartment()) )
                noticeRepository.save(notice);

            return scheduleResponseDto;

        } catch (Exception e) {
            throw new RuntimeException("work accept error");
        }

    }

    public List<WorkTempResponseDto> createWorkTemp(ScheduleResult scheduleJson) {

        if (!scheduleJson.isSuccess()) {
            System.out.println("🛑 스케줄 생성 실패 사유: \n" + scheduleJson.getFailureReason());
            throw new RuntimeException("유효하지 않은 근무입니다.");
        }

        // 이미 만들어져 있던 임시 근무는 삭제
        scheduleTempRepository.deleteAllByCategory(TempCategory.WORKING_TEMP);

        List<ScheduleTemp> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : scheduleJson.getSchedule().entrySet()) {
            String dateStr = entry.getKey(); // "2025-06-01"
            LocalDate baseDate = LocalDate.parse(dateStr);
            Map<String, String> shifts = entry.getValue();

            for (Map.Entry<String, String> shift : shifts.entrySet()) {
                String shiftType = shift.getKey();
                String nurseId = shift.getValue();

                Nurse nurse = nurseRepository.findByNo(nurseId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 간호사 UUID: " + nurseId));

                TempCategory category = TempCategory.WORKING_TEMP;
                LocalDateTime startTime;
                LocalDateTime endTime;

                switch (shiftType.toLowerCase()) {
                    case "day":
                        startTime = baseDate.atTime(6, 0);
                        endTime = baseDate.atTime(14, 0);
                        break;
                    case "evening":
                        startTime = baseDate.atTime(14, 0);
                        endTime = baseDate.atTime(22, 0);
                        break;
                    case "night":
                        startTime = baseDate.atTime(22, 0);
                        endTime = baseDate.plusDays(1).atTime(6, 0);
                        break;
                    default:
                        throw new IllegalArgumentException("알 수 없는 근무 유형: " + shiftType);
                }

                ScheduleTemp scheduleTemp = ScheduleTemp.builder()
                        .nurse(nurse)
                        .category(category)
                        .content("자동 생성된 GPT 근무 스케줄")
                        .startTime(startTime)
                        .endTime(endTime)
                        .build();

                result.add(scheduleTemp);
            }
        }

        // ✅ 1. 저장
        List<ScheduleTemp> savedList = scheduleTempRepository.saveAll(result);

        // ✅ 2. DTO 변환 후 반환
        List<WorkTempResponseDto> workTempResponseDtos = savedList.stream()
                .map(work -> {
                    Optional<String> codeLabel = commonCodeRepository
                            .findCodeLabelByCodeGroupAndCodeValue("schedule_temp_category", String.valueOf(work.getCategory()));
                    return WorkTempResponseDto.builder()
                            .workTempId(work.getId())
                            .nurseName(work.getNurse().getName())
                            .codeLabel(codeLabel.get())
                            .content(work.getContent())
                            .startTime(work.getStartTime())
                            .endTime(work.getEndTime())
                            .build();
                })
                .toList();

        return workTempResponseDtos;
    }
}
