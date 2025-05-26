package com.gandalp.gandalp.notice;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gandalp.gandalp.notice.dto.NoticeDto;
import com.gandalp.gandalp.notice.dto.NoticeCreateResponseDto;
import com.gandalp.gandalp.notice.dto.NoticeResponseDto;
import com.gandalp.gandalp.notice.service.NoticeService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {

	private final NoticeService noticeService;


	@Operation(summary = "긴급 공지사항 생성", description = "수간호사가 생성 버튼을 통해 공지사항을 작성할 수 있다.")
	@PostMapping
	@PreAuthorize("hasRole('HEAD_NURSE')")
	public ResponseEntity<?> createNoticeByHead(@RequestBody @Valid NoticeDto noticeDto){
		NoticeCreateResponseDto notice = null;

		try{

			notice = noticeService.createNoticeByHead(noticeDto);

		}catch(Exception e){
			return ResponseEntity.badRequest().body(e.getMessage());
		}

		return ResponseEntity.ok(notice);
	}


	@Operation(summary = "긴급 공지사항 삭제", description = "수간호사는 본인이 작성한 긴급 공지사항을 삭제할 수 있다.")
	@DeleteMapping
	@PreAuthorize("hasRole('HEAD_NURSE')")
	public ResponseEntity<?> deleteNotice(Long noticeId){

		try{
			noticeService.deleteNotice(noticeId);

		} catch (Exception e) {

			return ResponseEntity.badRequest().body(e.getMessage());
		}

		return ResponseEntity.ok("공지사항 삭제 완료!");

	}


	@Operation(summary = "공지사항 조회", description = "모든 간호사들은 본인이 소속된 과의 3일치 공지사항을 조회할 수 있다.")
	@GetMapping("/general")
	public ResponseEntity<?> getAllNotices(){
		List<NoticeResponseDto> noticeList = null;

		try {

			noticeList = noticeService.getGeneralNoticeList();

		}catch (Exception e){
			return ResponseEntity.badRequest().body(e.getMessage());
		}

		return ResponseEntity.ok(noticeList);
	}


	@Operation(summary = "긴급 공지사항 조회", description = "모든 간호사들은 본인이 소속된 과의 긴급 공지사항을 조회할 수 있다.")
	@GetMapping("/urgent")
	public ResponseEntity<?> getUrgentNotices(){
		List<NoticeResponseDto> urgentNoticeList = null;

		try {

			urgentNoticeList = noticeService.getUrgentNoticeList();

		}catch (Exception e){
			return ResponseEntity.badRequest().body(e.getMessage());
		}

		return ResponseEntity.ok(urgentNoticeList);
	}






}
