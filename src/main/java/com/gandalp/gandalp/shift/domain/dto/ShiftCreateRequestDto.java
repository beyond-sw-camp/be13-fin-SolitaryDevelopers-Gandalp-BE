package com.gandalp.gandalp.shift.domain.dto;

import com.gandalp.gandalp.shift.domain.entity.BoardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftCreateRequestDto {

    @NotNull
    private String content;

    private Long nurseId;


}
