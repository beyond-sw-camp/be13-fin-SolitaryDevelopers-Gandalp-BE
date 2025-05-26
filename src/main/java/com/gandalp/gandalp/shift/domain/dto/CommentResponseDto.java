package com.gandalp.gandalp.shift.domain.dto;

import com.gandalp.gandalp.shift.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {

    private Long commentId;

    private Long memberId;

    private Long boardId;

    private String content;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;

    private Long nurseId;


    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.createdBy = comment.getCreatedBy();
        this.memberId = comment.getMember() != null ? comment.getMember().getId() : null;
        this.boardId = comment.getBoard() != null ? comment.getBoard().getId() : null;
        this.updatedAt = comment.getUpdatedAt();
        this.updatedBy = comment.getUpdatedBy();
        this.nurseId = comment.getNurse() != null ? comment.getNurse().getId() : null;
    }

    public CommentResponseDto(Long commentId, String content, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.content = content;
        this.createdAt = createdAt;

    }

}
