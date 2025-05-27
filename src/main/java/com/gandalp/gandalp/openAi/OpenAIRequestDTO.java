package com.gandalp.gandalp.openAi;

import com.gandalp.gandalp.member.domain.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAIRequestDTO {
    private int year;     // ex. 2025
    private int month;    // ex. 6 (6월)
    private List<NurseScheduleInput> nurses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NurseScheduleInput {
        private String no; // 간호사들의 UUID

        private Type type;
        private int totalWorked;      // 총 근무 일수 (계산용)
        private int dayWorked;        // 데이 근무 횟수
        private int eveningWorked;    // 이브닝 근무 횟수
        private int nightWorked;      // 나이트 근무 횟수

        private List<LocalDate> offDays;  // 이번 달 오프 신청일
    }
}