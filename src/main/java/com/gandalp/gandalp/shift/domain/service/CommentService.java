package com.gandalp.gandalp.shift.domain.service;

import com.gandalp.gandalp.shift.domain.dto.CommentCreateRequestDto;
import com.gandalp.gandalp.shift.domain.dto.CommentResponseDto;
import com.gandalp.gandalp.shift.domain.dto.CommentUpdateDto;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
    // 댓글 작성
//    CommentResponseDto createComment(CommentCreateRequestDto shiftCreateRequestDto);

    CommentResponseDto createComment(CommentCreateRequestDto dto);

    // 댓글 수정
//    CommentResponseDto updateComment(CommentUpdateDto shiftUpdateDto, Long nurseId);

    CommentResponseDto updateComment(CommentUpdateDto dto, Long nurseId);

    // 댓글 조회
//    List<CommentResponseDto> findByBoardId(Long boardId);

    // 댓글 삭제
    void deleteComment(Long commentId, Long nurseId);

}
