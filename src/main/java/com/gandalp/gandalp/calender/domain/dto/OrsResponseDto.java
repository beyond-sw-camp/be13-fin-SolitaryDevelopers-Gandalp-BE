package com.gandalp.gandalp.calender.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrsResponseDto {

    private Long surgeryScheduleId;

    private Long roomId;

    private String content;

    private List<Long> nurseIds;

    private List<String> nurseNames;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
