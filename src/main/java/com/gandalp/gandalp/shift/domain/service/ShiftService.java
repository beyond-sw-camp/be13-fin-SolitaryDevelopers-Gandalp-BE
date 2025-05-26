package com.gandalp.gandalp.shift.domain.service;

import com.gandalp.gandalp.shift.domain.dto.ShiftCreateRequestDto;
import com.gandalp.gandalp.shift.domain.dto.ShiftDetailsResponseDto;
import com.gandalp.gandalp.shift.domain.dto.ShiftResponseDto;
import com.gandalp.gandalp.shift.domain.entity.SearchOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface ShiftService {

    // 교대 글 작성
    ShiftResponseDto createShift(ShiftCreateRequestDto shiftCreateRequestDto);

    // 교대 글 수정
//    ShiftResponseDto updateShift(ShiftUpdateDto shiftUpdateDto);

    // 교대 글 검색 조회
    Page<ShiftResponseDto> getSearchingAll(String keyword, SearchOption searchOption, Pageable pageable);

    // 교대 글 기본 조회
    Page<ShiftResponseDto> getAll(Pageable pageable);

    // 교대 글 단건 상세 조회
    ShiftDetailsResponseDto getShiftDetails(Long boardId);

    // 교대 글 삭제
//    void deleteShift(Long boardId);
    void deleteShift(Long boardId, Long nurseId);

    // 교대 요청 댓글 채택
    void submitComment(Long commentId);

}
