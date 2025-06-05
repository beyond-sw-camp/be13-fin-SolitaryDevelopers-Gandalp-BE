package com.gandalp.gandalp.schedule.domain.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.gandalp.gandalp.member.domain.entity.Type;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.hospital.domain.repository.DepartmentRepository;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.entity.Status;
import com.gandalp.gandalp.member.domain.entity.Type;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import com.gandalp.gandalp.schedule.domain.dto.StaticRequestDto;
import com.gandalp.gandalp.schedule.domain.dto.StaticsResponseDto;
import com.gandalp.gandalp.schedule.domain.entity.SelectOption;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NurseStaticsService {

	private final AuthService authService;
	private final NurseRepository nurseRepository;
	private final DepartmentRepository departmentRepository;
	private final ScheduleRepository scheduleRepository;


	public List<StaticsResponseDto> getWorkingStatics(StaticRequestDto staticRequestDto){

		// 1. 로그인한 회원 검증
		Member loginMember = authService.getLoginMember();

		// 2. 로그인한 회원의 부서 확인
		Department department = departmentRepository.findById(loginMember.getDepartment().getId()).orElseThrow(
			() -> new IllegalArgumentException("부서가 존재하지 않습니다.")
		);

		// 3. 해당하는 부서의 모든 간호사 리스트 조회
		List<Nurse> nurseList = nurseRepository.findByDepartmentAndTypeNot(department, Type.HEAD_NURSE);
		if (nurseList.isEmpty()){
			throw new IllegalArgumentException("해당 과에 간호사가 존재하지 않습니다.");
		}


		/// 4. 전달받은 값 처리
		SelectOption selectOption = staticRequestDto.getSelectOption();

		// 기본값
		int targetYear = LocalDate.now().getYear();
		int targetMonth = LocalDate.now().getMonthValue()-1;
		Integer targetQuarter = null;

		if (targetMonth == 0){
			targetYear --;
			targetMonth = 12;
		}

		if ( selectOption == SelectOption.MONTH ){
			targetMonth = staticRequestDto.getMonth();
			targetYear = staticRequestDto.getYear() == null ? targetYear : staticRequestDto.getYear();

		}else if (selectOption == SelectOption.QUARTER){
			targetQuarter = staticRequestDto.getQuarter();
			targetYear = staticRequestDto.getYear() == null ? targetYear : staticRequestDto.getYear();

		}else if( selectOption ==  SelectOption.YEAR){
			targetYear = staticRequestDto.getYear();
		}


		if ( targetYear < 2000 || targetYear > LocalDate.now().getYear() )
			throw new IllegalArgumentException("정확한 년도를 입력해주세요. ");

		if ( targetMonth > 12 || targetMonth < 0 )
			throw new IllegalArgumentException("1월 ~ 12월에서 골라주세요. ");


		List<StaticsResponseDto> allWorkingStatics = new ArrayList<>();

		// 모든 간호사에 대해 통합 통계를 조회함 (day/evening/night/off/surgery 포함)
		for (Nurse nurse : nurseList) {
			StaticsResponseDto nurseStatics = scheduleRepository.getNursesWorkingStatistics(nurse, selectOption, targetYear, targetMonth, targetQuarter);

			allWorkingStatics.add(nurseStatics);
		}

		return allWorkingStatics;

	}

}
