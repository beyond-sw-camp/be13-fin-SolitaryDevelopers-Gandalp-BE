package com.gandalp.gandalp.member.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gandalp.gandalp.auth.model.dto.CustomUserDetails;
import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.common.entity.CommonCode;
import com.gandalp.gandalp.common.repository.CommonCodeRepository;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.hospital.domain.repository.DepartmentRepository;
import com.gandalp.gandalp.member.domain.dto.NurseCurrentStatusDto;
import com.gandalp.gandalp.member.domain.dto.NurseResponseDto;
import com.gandalp.gandalp.member.domain.dto.NurseStatusResponseDto;
import com.gandalp.gandalp.member.domain.dto.NurseStatusUpdateDto;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.entity.Status;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleRepository;
import com.gandalp.gandalp.schedule.domain.repository.SurgeryScheduleRepository;
import com.gandalp.gandalp.schedule.domain.service.ScheduleService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class NurseService {

	private final NurseRepository nurseRepository;
	private final ScheduleService scheduleService;
	private final DepartmentRepository departmentRepository;
	private final ScheduleRepository scheduleRepository;
	private final SurgeryScheduleRepository surgeryScheduleRepository;
	private final AuthService authService;
	private final PasswordEncoder passwordEncoder;
	private final CommonCodeRepository commonCodeRepository;

	// 해당 과의 모든 간호사 조회 (페이징 처리 X)
	public List<NurseStatusResponseDto> getSimpleNurseList(Department department) {



		List<Nurse> nurseList = nurseRepository.findByDepartment(department);

		return nurseList.stream()
			.map(nurse -> new NurseStatusResponseDto(nurse.getId(), nurse.getName()))
			.toList();
	}



	///  간호사들의 현재 상태 조회
	public List<NurseCurrentStatusDto> getNurseStatus(){

		// 1. 로그인한 멤버 있는지 검증
		Member loginMember = authService.getLoginMember();

		// 2. 로그인한 사람의 부서 정보 가져오기
		Department dept = loginMember.getDepartment();

		// 3. 해당 부서의 간호사 리스트 가져오기
		List<Nurse> nurses = nurseRepository.findByDepartment(dept);

		// 4. 간호사의 현재상태 codeLabel 을 Map 형태로 가져오기
		Map<String, String> workingStatus = commonCodeRepository.findAllByCodeGroup("working_status")
																.stream()
																.collect(
																	Collectors.toMap(
																		CommonCode::getCodeValue,
																		CommonCode::getCodeLabel
																	));

		return nurses.stream()
			.map(nurse -> {
				String codeValue = nurse.getWorkingStatus().name();
				String codeLabel = workingStatus.getOrDefault(codeValue, codeValue);

				return NurseCurrentStatusDto.builder()
					.id(nurse.getId())
					.name(nurse.getName())
					.codeLabel(codeLabel)
					.build();

			}).toList();
	}

	// 간호사들 현재 상태 수정
	@Transactional
	public NurseCurrentStatusDto updateNurseStatus(NurseStatusUpdateDto request){

		// 1. 로그인했는지 검증
		authService.getLoginMember();

		// 2. request 에서의 long id 와 그 사람의 비밀번호 맞는지 검증
		Nurse nurse = nurseRepository.findById(request.getNurseId()).orElseThrow(
			() -> new IllegalArgumentException("해당 간호사가 존재하지 않습니다.")
		);


		if(!passwordEncoder.matches(request.getPassword(), nurse.getPassword())){
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}


		// 3. 근무 상태 수정
		Status workingStatus = request.getWorkingStatus();

		nurse.updateWorkingStatus(workingStatus);
		log.info("수정! ");

		return new NurseCurrentStatusDto(nurse);
	}


	///  간호사들 근무 상태 자동 변환
	@Scheduled(cron = "0 0 * * * *") // 1시간마다 갱신
	@Transactional
	public void autoUpdateNurseStatus(){
		// 과, 병원 필요없고 DB 에 존재하는 모든 간호사들의 일정 확인 후 상태 전환


		LocalDateTime now = LocalDateTime.now();
		// 1. 모든 간호사 조회
		// 2. 현재시간을 기준으로 수술중인지 체크
		// 3. 현재시간을 기준으로 근무중인지 체크

		List<Nurse> allNurse = nurseRepository.findAll();
		for(Nurse nurse: allNurse){


			Status before = nurse.getWorkingStatus();

			// 1. 수술중인지
			if (surgeryScheduleRepository.isNurseInSurgery(nurse.getId(), now)){
				nurse.updateWorkingStatus(Status.IN_SURGERY);
				continue;
			}

			// 2. 일반 진료근무중인지 확인
			if (scheduleRepository.findCurrentSchedule(nurse.getId(), now)){
				nurse.updateWorkingStatus(Status.ON);
				continue;
			}

			// 3. 둘다 아니면 off !
			nurse.updateWorkingStatus(Status.OFF);


			Status after = nurse.getWorkingStatus();
			if (!before.equals(after)) {
				nurse.updateWorkingStatus(after);
				log.info("간호사 [{}] 상태 변경: {} → {}", nurse.getName(), before, after);
			}

		}
	}
}

