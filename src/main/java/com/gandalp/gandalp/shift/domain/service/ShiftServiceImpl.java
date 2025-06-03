package com.gandalp.gandalp.shift.domain.service;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.common.entity.CommonCode;
import com.gandalp.gandalp.common.repository.CommonCodeRepository;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.mail.MailService;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import com.gandalp.gandalp.schedule.domain.entity.Schedule;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleRepository;
import com.gandalp.gandalp.schedule.domain.service.ScheduleService;
import com.gandalp.gandalp.shift.ScheduleValidator;
import com.gandalp.gandalp.shift.domain.dto.CommentResponseDto;
import com.gandalp.gandalp.shift.domain.dto.ShiftCreateRequestDto;
import com.gandalp.gandalp.shift.domain.dto.ShiftDetailsResponseDto;
import com.gandalp.gandalp.shift.domain.dto.ShiftResponseDto;
import com.gandalp.gandalp.shift.domain.entity.Board;
import com.gandalp.gandalp.shift.domain.entity.BoardStatus;
import com.gandalp.gandalp.shift.domain.entity.Comment;
import com.gandalp.gandalp.shift.domain.entity.SearchOption;
import com.gandalp.gandalp.shift.domain.repository.CommentRepository;
import com.gandalp.gandalp.shift.domain.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;

    private final CommonCodeRepository commonCodeRepository;

    private final NurseRepository nurseRepository;
    private final PasswordEncoder passwordEncoder;

    private final CommentRepository commentRepository;
    private final AuthService authService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleValidator scheduleValidator;
    private final MailService mailService;

    private final ScheduleService scheduleService;

    // 교대 요청 댓글 채택
    @Override
    @Transactional
    public void submitComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        Board board = comment.getBoard();
        if (board == null) {
            throw new IllegalArgumentException("댓글에 해당하는 게시글이 없습니다.");
        }
        if (board.getBoardStatus() == BoardStatus.Completed) {
            throw new IllegalStateException("이 게시물은 이미 채택된 상태입니다.");
        }

        // 1. 게시글 nurse와 댓글 nurse
        Nurse boardNurse = board.getNurse();
        Nurse commentNurse = comment.getNurse();

        // 2. 게시글의 일정 찾기 (board.content 파싱)
        ScheduleValidator.ParsedShift boardShift = scheduleValidator.parseContentToShiftTime(board.getContent());
        // 3. 댓글의 일정 찾기 (comment.content 파싱)
        ScheduleValidator.ParsedShift commentShift = scheduleValidator.parseContentToShiftTime(comment.getContent());

        // *** 교대 종류(shiftType) 일치 검증 ***
        String boardShiftType = boardShift.shiftType;    // 예: "데이", "이브닝", "나이트"
        String commentShiftType = commentShift.shiftType;

        // 데이/이브닝은 서로 교환 가능, 나이트는 나이트만
        boolean isBoardDayOrEvening = boardShiftType.equals("데이") || boardShiftType.equals("이브닝");
        boolean isCommentDayOrEvening = commentShiftType.equals("데이") || commentShiftType.equals("이브닝");
        boolean isBoardNight = boardShiftType.equals("나이트");
        boolean isCommentNight = commentShiftType.equals("나이트");

        if (isBoardDayOrEvening) {
            if (!isCommentDayOrEvening) {
                throw new IllegalArgumentException("데이/이브닝 교대 요청에는 데이/이브닝 댓글만 교환할 수 있습니다.");
            }
        } else if (isBoardNight) {
            if (!isCommentNight) {
                throw new IllegalArgumentException("나이트 교대 요청에는 나이트 댓글만 교환할 수 있습니다.");
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 교대 타입입니다.");
        }

        Schedule boardSchedule = scheduleRepository.findByNurseIdAndStartTimeLessThanEqualAndEndTimeGreaterThan(
                boardNurse.getId(), boardShift.startTime, boardShift.startTime
        ).orElseThrow(() -> new RuntimeException("게시글 작성자의 일정이 없습니다."));

        Schedule commentSchedule = scheduleRepository.findByNurseIdAndStartTimeLessThanEqualAndEndTimeGreaterThan(
                commentNurse.getId(), commentShift.startTime, commentShift.startTime
        ).orElseThrow(() -> new RuntimeException("댓글 작성자의 일정이 없습니다."));

        Nurse temp = boardSchedule.getNurse();
        boardSchedule.setNurse(commentSchedule.getNurse());
        commentSchedule.setNurse(temp);

        scheduleRepository.save(boardSchedule);
        scheduleRepository.save(commentSchedule);

        scheduleService.recalculateCurrentMonthStatistics(boardSchedule.getStartTime(), boardSchedule.getNurse());
        scheduleService.recalculateCurrentMonthStatistics(commentSchedule.getStartTime(), commentSchedule.getNurse());


        board.completeRequest();
        shiftRepository.save(board);

        mailService.sendSimpleMailMessage(boardNurse, commentNurse);
    }

    // 공통 코드 변환 메서드
    private ShiftResponseDto toDto(Board board) {
        String label = commonCodeRepository
                .findByCodeGroupAndCodeValue("board_status", String.valueOf(board.getBoardStatus()))
                .map(CommonCode::getCodeLabel)
                .orElse(null);

        // 라벨을 포함하는 생성자(혹은 setter)로 DTO 생성
        ShiftResponseDto dto = new ShiftResponseDto(board, label);
        dto.setBoardStatusLabel(label);
        return dto;
    }

    

    // 교대 요청 글 C
    @Override
    public ShiftResponseDto createShift(ShiftCreateRequestDto shiftCreateRequestDto) {


        Member member = authService.getLoginMember();
        Department department = member.getDepartment();
        List<Comment> comments = new ArrayList<>();

        Nurse nurse = nurseRepository.findById(shiftCreateRequestDto.getNurseId())
                .orElseThrow(() -> new RuntimeException("간호사를 찾을 수 없습니다."));

        ScheduleValidator.ParsedShift parsed = scheduleValidator.parseContentToShiftTime(shiftCreateRequestDto.getContent());
        if (!scheduleValidator.existsScheduleForNurse(shiftCreateRequestDto.getNurseId(), parsed.startTime)) {
            throw new RuntimeException("해당 시간에 근무 일정이 없습니다.");
        }

        // 3. 게시글 생성
        Board board = Board.builder()
                .content(shiftCreateRequestDto.getContent())
                .boardStatus(BoardStatus.Waiting)
                .comments(comments)
                .department(department)
                .member(member)
                .nurse(nurse)
                .build();
        shiftRepository.save(board);

        Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("board_status", String.valueOf(board.getBoardStatus()));
        if (codeLabel.isEmpty()) {
            throw new RuntimeException("codeLabel is empty");
        }
        return new ShiftResponseDto(board, codeLabel.get());
    }

    // 교대 요청 글 R
    // 교대 요청 글 검색 조회
    @Override
    public Page<ShiftResponseDto> getSearchingAll(String keyword, SearchOption searchOption, Pageable pageable) {
        // 1. 로그인한 사용자 조회
        Member member = authService.getLoginMember();
        Department department = member.getDepartment();

        // 2. 부서 기준으로 검색
        return shiftRepository.getSearchingAllByDepartment(department, keyword, searchOption, pageable);
    }


    // 교대 요청 글 기본 조회
    @Override
    public Page<ShiftResponseDto> getAll(Pageable pageable) {
        Member member = authService.getLoginMember();
        Department department = member.getDepartment();

        // JPA 기본 메서드 → 커스텀 QueryDSL 메서드로 변경
        return shiftRepository.getAllByDepartment(department, pageable);
    }


    // 교대 요청 글 단건 상세 조회
    public ShiftDetailsResponseDto getShiftDetails(Long boardId) {
        Board board = commentRepository.findByIdWithCommentsAndNurse(boardId)
                .orElseThrow(() -> new RuntimeException("해당 글이 없습니다."));


        List<CommentResponseDto> commentDtos = board.getComments().stream()
                .map(CommentResponseDto::new)
//                .map(comment -> new CommentResponseDto(comment.getId(), comment.getContent(), comment.getCreatedAt()))
                .collect(Collectors.toList());

        Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("board_status", String.valueOf(board.getBoardStatus()));

        if(codeLabel.isEmpty()) {
            throw new RuntimeException("codeLabel is empty");
        }

        // nurseName 세팅 추가
        return ShiftDetailsResponseDto.builder()
                .boardId(board.getId())
                .content(board.getContent())
                .codeLabel(codeLabel.get())
                .comments(commentDtos)
                .nurseId(board.getNurse() != null ? board.getNurse().getId() : null)
                .nurseName(board.getNurse() != null ? board.getNurse().getName() : null) // ← 이 부분!
                .build();
    }

//    public ShiftDetailsResponseDto getShiftDetails(Long boardId) {
//        Board board = shiftRepository.findByIdWithComments(boardId)
//                .orElseThrow(() -> new RuntimeException("해당 글을 찾을 수 없습니다."));
//
//        List<CommentResponseDto> commentDtos = board.getComments().stream()
//                .map(comment -> new CommentResponseDto(comment.getId(), comment.getContent(), comment.getCreatedAt()))
//                .collect(Collectors.toList());
//
//        Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("board_status", String.valueOf(board.getBoardStatus()));
//
//        if(codeLabel.isEmpty()) {
//            throw new RuntimeException("codeLabel is empty");
//        }
//
//        return new ShiftDetailsResponseDto(
//                board.getId(),
//                board.getContent(),
//                codeLabel.get(),
//                commentDtos,
//                board.getNurse() != null ? board.getNurse().getId() : null
//        );
//    }


    // 교대 요청 글 D
    @Override
    public void deleteShift(Long boardId, Long nurseId) {
        Board board = shiftRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("board is empty"));
        if (!board.getNurse().getId().equals(nurseId)) {
            throw new RuntimeException("정보가 일치하지 않습니다.");
        }
        shiftRepository.deleteById(boardId);
    }

}

