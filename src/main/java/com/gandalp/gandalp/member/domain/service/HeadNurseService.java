package com.gandalp.gandalp.member.domain.service;

import com.gandalp.gandalp.auth.model.dto.CustomUserDetails;
import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.member.domain.dto.NurseRequestDto;
import com.gandalp.gandalp.member.domain.dto.NurseResponseDto;
import com.gandalp.gandalp.member.domain.dto.NurseUpdateDto;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.entity.NurseSearchOption;
import com.gandalp.gandalp.member.domain.entity.Status;
import com.gandalp.gandalp.member.domain.repository.MemberRepository;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HeadNurseService {

    private final PasswordEncoder passwordEncoder;
    private final NurseRepository nurseRepository;
    private final AuthService authService;

    // 수간호사가 간호사 생성
    @Transactional
    public void createNurse(NurseRequestDto reqDto){

        // 1. 현재 로그인한 사용자
        Member loginMember = authService.getLoginMember();

        // 2. 부서 가져오기
        Department department = loginMember.getDepartment();


        // 3. 이메일 중복 체크
        authService.validateDuplicateEmail(reqDto.getEmail());


        // 4. 비밀번호 암호화
        String password = passwordEncoder.encode(reqDto.getPassword());




        // 5. nurse 생성
        Nurse nurse = Nurse.builder()
                            .department(department)
                            .name(reqDto.getName())
                            .email(reqDto.getEmail())
                            .type(reqDto.getType())
                            .password(password)
                            .workingStatus(Status.OFF)
                            .build();


        nurseRepository.save(nurse);
        department.countUp();

        log.info("간호사 생성 완료 ~~");

    }


    // 수간호사가 간호사 수정
    @Transactional
    public NurseResponseDto updateNurse(Long nurseId, NurseUpdateDto dto){


        // 1. nurse 존재하는지 검증
        Nurse nurse = nurseRepository.findById(nurseId).orElseThrow(
                ()-> new EntityNotFoundException("해당하는 간호사가 존재하지 않습니다.")
        );

        // 2. 수정
        nurse.update(dto);

        return new NurseResponseDto(nurse);
    }



    // 수간호사가 간호사 삭제
    @Transactional
    public void deleteNurse(Long nurseId) {


        // 1. 간호사가 DB에 존재하는지 검증
        Nurse nurse = nurseRepository.findById(nurseId).orElseThrow(
                () -> new EntityNotFoundException("해당하는 간호사가 존재하지 않습니다.")
        );

        // 2. 부서 인원 수 변경
        Department department = nurse.getDepartment();
        if ( department.getNurseCount() <= 0 ){
            throw new IllegalArgumentException("간호사 인원 수가 0명 이하가 될 수 없습니다.");
        }

        department.countDown();

        nurseRepository.deleteById(nurseId);
    }


    // 해당 과의 모든 간호사 조회 (수간호사 모두 가능) -> 페이징 처리
    public Page<NurseResponseDto> getAll(String keyword, NurseSearchOption searchOption, Pageable pageable){

        Member loginMember = authService.getLoginMember();
        Long deptId = loginMember.getDepartment().getId();

        Page<NurseResponseDto> nurseList = nurseRepository.getAll(keyword, searchOption, pageable, deptId);

        return nurseList;
    }

    public NurseResponseDto getOneNurse(Long nurseId){

       // 1. 간호사 조회
       Nurse nurse = nurseRepository.findById(nurseId).orElseThrow(
               () -> new EntityNotFoundException("해당하는 간호사가 존재하지 않습니다.")
       );
       return new NurseResponseDto(nurse);
    }

}
