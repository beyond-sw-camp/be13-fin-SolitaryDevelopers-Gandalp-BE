package com.gandalp.gandalp.schedule.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class WorkScheduleResponseDto {

    private Long workScheduleId;

    private Long nurseId;

    private String nurseName;

    private String codeLabel;

    private String content;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
