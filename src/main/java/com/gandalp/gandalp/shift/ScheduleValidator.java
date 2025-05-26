package com.gandalp.gandalp.shift;

import com.gandalp.gandalp.schedule.domain.repository.ScheduleRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ScheduleValidator {
    private final ScheduleRepository scheduleRepository;

    public ScheduleValidator(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public boolean existsScheduleForNurse(Long nurseId, LocalDateTime startTime) {
        return scheduleRepository.existsByNurseIdAndStartTimeLessThanEqualAndEndTimeGreaterThan(nurseId, startTime, startTime);
    }

    // schedule 존재 여부 검증
    private static class ShiftTimeRange {
        int startHour;
        int endHour;
        // 나이트는 endHour가 6(익일)임에 주의
        ShiftTimeRange(int startHour, int endHour) {
            this.startHour = startHour;
            this.endHour = endHour;
        }
    }

    private static final Map<String, ShiftTimeRange> SHIFT_TIME_MAP = Map.of(
            "데이", new ShiftTimeRange(6, 14),
            "이브닝", new ShiftTimeRange(14, 22),
            "나이트", new ShiftTimeRange(22, 6)
    );

    // content 파싱 및 LocalDateTime 변환
    public static class ParsedShift {
        public LocalDateTime startTime;
        LocalDateTime endTime;
    }
    public ParsedShift parseContentToShiftTime(String content) {
        // 예시 content: "5월 22일 이브닝"
        Pattern pattern = Pattern.compile("(\\d{1,2})월 (\\d{1,2})일 (데이|이브닝|나이트)");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("content 형식이 올바르지 않습니다.");
        }
        int month = Integer.parseInt(matcher.group(1));
        int day = Integer.parseInt(matcher.group(2));
        String timeStr = matcher.group(3);

        ShiftTimeRange range = SHIFT_TIME_MAP.get(timeStr);
        if (range == null) throw new IllegalArgumentException("타임 정보가 올바르지 않습니다.");

        int year = LocalDate.now().getYear(); // 올해 기준

        LocalDateTime startTime = LocalDateTime.of(year, month, day, range.startHour, 0);
        LocalDateTime endTime;
        if ("나이트".equals(timeStr)) {
            // 나이트는 익일 6시까지
            LocalDate endDate = LocalDate.of(year, month, day).plusDays(1);
            endTime = LocalDateTime.of(endDate, LocalTime.of(range.endHour, 0));
        } else {
            endTime = LocalDateTime.of(year, month, day, range.endHour, 0);
        }
        ParsedShift result = new ParsedShift();
        result.startTime = startTime;
        result.endTime = endTime;
        return result;
    }
}
