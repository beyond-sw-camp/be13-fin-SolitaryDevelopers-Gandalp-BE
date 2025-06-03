package com.gandalp.gandalp.shift.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftDetailsResponseDto {

    @NotNull
    private Long boardId;

    @NotNull
    private String content;

    @NotNull
    private String codeLabel;

    @NotNull
    List<CommentResponseDto> comments;

    private Long nurseId;

    private String nurseName;
}
