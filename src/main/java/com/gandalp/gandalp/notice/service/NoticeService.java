package com.gandalp.gandalp.notice.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.common.repository.CommonCodeRepository;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.notice.dto.NoticeCreateResponseDto;
import com.gandalp.gandalp.notice.dto.NoticeDto;
import com.gandalp.gandalp.notice.dto.NoticeResponseDto;
import com.gandalp.gandalp.notice.entity.Notice;
import com.gandalp.gandalp.notice.entity.NoticeCategory;
import com.gandalp.gandalp.notice.repository.NoticeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {

	private final AuthService authService;
	private final NoticeRepository noticeRepository;
	private final CommonCodeRepository commonCodeRepository;

	@Transactional
	public NoticeCreateResponseDto createNoticeByHead(NoticeDto createDto){

		Member loginMember = authService.getLoginMember();

		Notice notice = Notice.builder()
			.category(NoticeCategory.URGENT)
			.content(createDto.getContent())
			.department(loginMember.getDepartment())
			.build();

		noticeRepository.save(notice);

		NoticeCreateResponseDto noticeDto = NoticeCreateResponseDto.builder()
			.id(notice.getId())
			.category(notice.getCategory().toString())
			.content(notice.getContent())
			.departmentName(notice.getDepartment().getName())
			.build();

		return noticeDto;
	}

	// 공지사항 삭제
	@Transactional
	public void deleteNotice(Long noticeId){

		Member loginMember = authService.getLoginMember();

		// 1. DB에 공지사항 존재하는지 검증하기
		Notice notice = noticeRepository.findById(noticeId).orElseThrow(
			() -> new IllegalArgumentException("해당 공지사항은 존재하지 않습니다.")
		);

		// 2. 해당 간호사가 긴급 공지사항을 작성한 간호사인지 검증
		String writer = notice.getCreatedBy();
		if (!writer.equals(loginMember.getAccountId())){
			throw new IllegalArgumentException("본인이 작성한 공지사항이 아닙니다.");
		}

		noticeRepository.delete(notice);
	}


	// 일반 공지사항 조회
	public List<NoticeResponseDto> getGeneralNoticeList(){


		// 1. 로그인 했는지 검증
		Member loginMember = authService.getLoginMember();

		// 2. 로그인한 유저의 과 정보 가져오기
		Department department = loginMember.getDepartment();

		// 3. 오늘 날짜 정보 가져오기
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = now.minusDays(3);

		// 4. (오늘 날짜 - 3)일치 일반 공지사항을 가져오기
		List<Notice> allList = noticeRepository.findAllByCategoryAndCreatedAtBetweenAndDepartment(NoticeCategory.GENERAL, start, now,department);


		// 5. code label
		return allList.stream()
			.sorted(Comparator.comparing(Notice::getCreatedAt).reversed())
			.map(notice -> {
				Optional<String> noticeCategory = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue(
					"notice_category", String.valueOf(notice.getCategory()));

				return NoticeResponseDto.builder()
					.noticeId(notice.getId())
					.codeLabel(noticeCategory.orElse("알 수 없음"))
					.content(notice.getContent())
					.build();

			})
			.toList();

	}

	public List<NoticeResponseDto> getUrgentNoticeList(){


		// 1. 로그인 했는지 검증
		Member loginMember = authService.getLoginMember();

		// 2. 로그인한 유저의 과 정보 가져오기
		Department department = loginMember.getDepartment();


		// 3. 오늘 날짜 정보 가져오기
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = now.minusDays(3);

		// 4. 긴급 공지사항 조회 - 로그인한 유저의 해당 과의 카테고리만 조회 가능
		List<Notice> allList = noticeRepository.findAllByDepartmentAndCategory(department, NoticeCategory.URGENT);


		// 5. code label 해서 반환
		return allList.stream()
			.sorted(Comparator.comparing(Notice::getCategory))
			.map(notice -> {
				Optional<String> noticeCategory = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue(
					"notice_category", String.valueOf(notice.getCategory()));

				return NoticeResponseDto.builder()
					.noticeId(notice.getId())
					.codeLabel(noticeCategory.orElse("알 수 없음"))
					.content(notice.getContent())
					.build();

			})
			.toList();

	}

	// 생성된지 3일된 일반 공지사항은 자동삭제된다.
	@Transactional
	@Scheduled(cron = "0 0 0 * * *") // 매일 자정에 자동 실행
	public void autoDelete(){

		LocalDateTime limit = LocalDateTime.now().minusDays(3);

		List<Notice> deleteList = noticeRepository.findAllByCategoryAndCreatedAtBefore(
			NoticeCategory.GENERAL, limit);

		noticeRepository.deleteAll(deleteList);

	}


}
