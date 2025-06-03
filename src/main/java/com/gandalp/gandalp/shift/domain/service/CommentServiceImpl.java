package com.gandalp.gandalp.shift.domain.service;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import com.gandalp.gandalp.shift.ScheduleValidator;
import com.gandalp.gandalp.shift.domain.dto.CommentCreateRequestDto;
import com.gandalp.gandalp.shift.domain.dto.CommentResponseDto;
import com.gandalp.gandalp.shift.domain.dto.CommentUpdateDto;
import com.gandalp.gandalp.shift.domain.entity.Board;
import com.gandalp.gandalp.shift.domain.entity.Comment;
import com.gandalp.gandalp.shift.domain.repository.CommentRepository;
import com.gandalp.gandalp.shift.domain.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final ShiftRepository shiftRepository;

    private final NurseRepository nurseRepository;
    private final AuthService authService;
    private final ScheduleValidator scheduleValidator;

    // 댓글 C
    @Override
    public CommentResponseDto createComment(CommentCreateRequestDto dto) {
        // 댓글의 교대 종류 파싱
        ScheduleValidator.ParsedShift commentShift = scheduleValidator.parseContentToShiftTime(dto.getContent());
        if (!scheduleValidator.existsScheduleForNurse(dto.getNurseId(), commentShift.startTime)) {
            throw new RuntimeException("해당 시간에 근무 일정이 없습니다.");
        }

        Member member = authService.getLoginMember();

        Long nurseId = dto.getNurseId();
        Nurse nurse = nurseRepository.findById(nurseId)
                .orElseThrow(() -> new IllegalArgumentException("간호사를 찾을 수 없습니다."));

        Long boardId = dto.getBoardId();
        Board board = shiftRepository.findById(dto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        if (board.getNurse().getId().equals(dto.getNurseId())) {
            throw new IllegalArgumentException("자신의 게시글에는 댓글을 작성할 수 없습니다.");
        }

        // *** 교대 종류 검증 추가 ***
        ScheduleValidator.ParsedShift boardShift = scheduleValidator.parseContentToShiftTime(board.getContent());
        String boardShiftType = boardShift.shiftType;    // 예: "데이", "이브닝", "나이트"
        String commentShiftType = commentShift.shiftType;

        boolean isBoardDayOrEvening = boardShiftType.equals("데이") || boardShiftType.equals("이브닝");
        boolean isCommentDayOrEvening = commentShiftType.equals("데이") || commentShiftType.equals("이브닝");
        boolean isBoardNight = boardShiftType.equals("나이트");
        boolean isCommentNight = commentShiftType.equals("나이트");

        if (isBoardDayOrEvening) {
            if (!isCommentDayOrEvening) {
                throw new IllegalArgumentException("데이/이브닝 교대 요청에는 데이/이브닝 댓글만 작성할 수 있습니다.");
            }
        } else if (isBoardNight) {
            if (!isCommentNight) {
                throw new IllegalArgumentException("나이트 교대 요청에는 나이트 댓글만 작성할 수 있습니다.");
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 교대 타입입니다.");
        }

        // 댓글 생성 및 저장
        Comment comment = Comment.builder()
                .content(dto.getContent())
                .member(member)
                .board(board)
                .nurse(nurse)
                .build();
        commentRepository.save(comment);
        return new CommentResponseDto(comment);
    }


    // 댓글 U

    @Override
    public CommentResponseDto updateComment(CommentUpdateDto dto, Long nurseId) {
        Comment comment = commentRepository.findById(dto.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        if (!comment.getNurse().getId().equals(dto.getNurseId())) {
            throw new IllegalArgumentException("작성자 정보가 일치하지 않습니다.");
        }
        comment.update(dto);
        return new CommentResponseDto(comment);
    }



    // 댓글 D
    @Override
    public void deleteComment(Long commentId, Long nurseId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        if (!comment.getNurse().getId().equals(nurseId)) {
            throw new IllegalArgumentException("작성자 정보가 일치하지 않습니다.");
        }
        commentRepository.deleteById(commentId);
    }

}


