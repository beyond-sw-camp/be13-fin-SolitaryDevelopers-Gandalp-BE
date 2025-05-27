package com.gandalp.gandalp.schedule.domain.dto;

import com.gandalp.gandalp.schedule.domain.entity.ScheduleTemp;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class WorkTempResponseDto {

    private final Long workTempId;

    private final String nurseName;

    private final String codeLabel;

    private final String content;

    private final LocalDateTime startTime;

    private final LocalDateTime endTime;

}
