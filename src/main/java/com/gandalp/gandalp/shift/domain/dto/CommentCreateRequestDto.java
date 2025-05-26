package com.gandalp.gandalp.shift.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDto {

    @NotNull
    private Long boardId;

    @NotNull
    private String content;

    private Long nurseId;

}
