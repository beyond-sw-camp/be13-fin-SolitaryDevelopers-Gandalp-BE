package com.gandalp.gandalp.shift;

import com.gandalp.gandalp.shift.domain.dto.ShiftCreateRequestDto;
import com.gandalp.gandalp.shift.domain.dto.ShiftDetailsResponseDto;
import com.gandalp.gandalp.shift.domain.dto.ShiftResponseDto;
import com.gandalp.gandalp.shift.domain.entity.SearchOption;
import com.gandalp.gandalp.shift.domain.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    // 교대 요청 댓글 채택
    @Operation(summary = "교대 요청 댓글이 채택")
    @PostMapping("comments/{comment-id}/submit")
    public ResponseEntity<?> submitComment(@PathVariable("comment-id") Long commentId,
                                            @RequestParam Long boardId) {
        try {
            shiftService.submitComment(commentId); // 내부에서 트랜잭션 처리
            return ResponseEntity.ok("교대 요청이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // 교대 요청 글 C
    @Operation(summary = "교대 요청 글 등록", description = "교대 요청 글 등록")
    @PostMapping("/create")
    public ResponseEntity<?> createShift(
            @RequestBody @Valid ShiftCreateRequestDto shiftCreateRequestDto) {

        try {
            ShiftResponseDto shiftResponseDto = shiftService.createShift(shiftCreateRequestDto);
            return ResponseEntity.ok().body(shiftResponseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 교대 요청 글 R

    @Operation(summary = "교대 요청 글 검색 & 조회", description = "교대 요청 글 검색 & 조회")
    @GetMapping("/search")
    public ResponseEntity<?> searchAllShifts(

            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SearchOption searchOption,
            @PageableDefault(size = 10, page = 0) Pageable pageable){

        try {

            Page<ShiftResponseDto> allShifts = shiftService.getSearchingAll(keyword, searchOption, pageable);
            return ResponseEntity.ok().body(allShifts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "교대 요청 글 목록 기본 조회", description = "교대 요청 글 리스트 기본 조회")
    @GetMapping
    public ResponseEntity<?> showShiftsByMember(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")); // boardId 기준 역순
        Page<ShiftResponseDto> shifts = shiftService.getAll(pageable);
        return ResponseEntity.ok().body(shifts);
    }

    @Operation(summary = "교대 요청 글 단건 상세 조회", description = "boardId로 교대 요청 글 & 댓글 조회")
    @GetMapping("/{board-id}")
    public ResponseEntity<?> showShiftDetailsByBoard(
            @PathVariable("board-id") Long boardId) {
        try {
            ShiftDetailsResponseDto shiftDetails = shiftService.getShiftDetails(boardId);
            return ResponseEntity.ok().body(shiftDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // 교대 글 D
    @Operation(summary = "교대 요청 글 삭제", description = "교대 요청 글 삭제")
    @DeleteMapping("/{board-id}")
    public ResponseEntity<?> deleteShift(@PathVariable("board-id") Long boardId, @RequestBody Map<String, Long> body) {
        try {
            Long nurseId = body.get("nurseId");
            shiftService.deleteShift(boardId, nurseId);
            return ResponseEntity.ok().body("교대 요청 글이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


//    @DeleteMapping("/{board-id}")
//    public ResponseEntity<?> deleteShift(@PathVariable("board-id") Long boardId, Long nurseId) {
//
//        try {
//            shiftService.deleteShift(boardId, nurseId);
//
////            return ResponseEntity.status(HttpStatus.OK).body("교대 요청 글이 삭제되었습니다.");
//            return ResponseEntity.ok().body("교대 요청 글이 삭제되었습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}
