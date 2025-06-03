package com.gandalp.gandalp.member;

import java.util.List;
import java.util.stream.Collectors;

import com.gandalp.gandalp.member.domain.dto.NurseNameEmailDto;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.member.domain.dto.NurseCurrentStatusDto;
import com.gandalp.gandalp.member.domain.dto.NurseRequestDto;
import com.gandalp.gandalp.member.domain.dto.NurseResponseDto;
import com.gandalp.gandalp.member.domain.dto.NurseStatusResponseDto;
import com.gandalp.gandalp.member.domain.dto.NurseStatusUpdateDto;
import com.gandalp.gandalp.member.domain.dto.NurseUpdateDto;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.NurseSearchOption;
import com.gandalp.gandalp.member.domain.service.HeadNurseService;
import com.gandalp.gandalp.member.domain.service.NurseService;
import com.gandalp.gandalp.schedule.domain.service.ScheduleService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/nurses")
@RequiredArgsConstructor
public class NurseController {

    private final NurseService nurseService;
    private final HeadNurseService headNurseService;
    private final AuthService authService;

    private final NurseRepository nurseRepository;

    // 간호사 생성
    @Operation(summary = "간호사 생성", description = "수간호사가 새로운 간호사를 생성합니다.")
    @PostMapping
    @PreAuthorize("hasRole('HEAD_NURSE')")
    public ResponseEntity<String> createNurse(@RequestBody NurseRequestDto dto) {

        try {
            // 나중에 security 엔드포인트 추가하깅
            headNurseService.createNurse(dto);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());

        }
        return ResponseEntity.ok("간호사 생성");
    }

    // 간호사 수정
    @Operation(summary = "간호사 수정", description = "수간호사가 간호사 정보를 수정합니다.")
    @PutMapping("/{nurseId}")
    @PreAuthorize("hasRole('HEAD_NURSE') and hasPermission(#nurseId, 'HEAD_NURSE_ACCESS')")
    public ResponseEntity<?> updateNurse(@PathVariable Long nurseId, @RequestBody NurseUpdateDto dto) {

        // 나중에 security 엔드포인트 추가하깅

        NurseResponseDto resDto = null;
        try{
            resDto = headNurseService.updateNurse(nurseId, dto);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok(resDto);
    }

    // 간호사 삭제
    @Operation(summary = "간호사 삭제", description = "수간호사가 간호사를 삭제합니다.")
    @DeleteMapping("/{nurseId}")
    @PreAuthorize("hasRole('HEAD_NURSE') and hasPermission(#nurseId, 'HEAD_NURSE_ACCESS')")
    public ResponseEntity<String> deleteNurse(@PathVariable Long nurseId) {

        try{
            headNurseService.deleteNurse(nurseId);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("간호사가 삭제되었습니다.");
    }

    // 간호사 단 건 조회
    @GetMapping("/{nurseId}")
    @PreAuthorize("hasPermission(#nurseId, 'NURSE_ACCESS')")
    public ResponseEntity<NurseResponseDto> getOneNurse(@PathVariable Long nurseId){

        NurseResponseDto responseDto = headNurseService.getOneNurse(nurseId);

        return ResponseEntity.ok(responseDto);
    }

    //간호사 전체 조회
    @Operation(summary = "간호사 전체 조회", description = "수간호사가 소속된 부서의 간호사들을 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('HEAD_NURSE')")
    public ResponseEntity<?> getAllNurses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) NurseSearchOption searchOption, // 검색 옵션
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<NurseResponseDto> allNurses = null;

        try{

            allNurses = headNurseService.getAll(keyword, searchOption, pageable);

        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }


        return ResponseEntity.ok(allNurses);
    }

    // 간호사들의 현재 상태 조회
    // 로그인한 계정의 과를 가지고 와서 모든 간호사들의 상태 조회 list 로 반환
    @Operation(summary = "간호사들 현재 상태 조회", description = "해당 과의 모든 간호사들의 현재 근무상태를 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<?> getNurseStatus() {

        List<NurseCurrentStatusDto> status = null;

        try {
            status = nurseService.getNurseStatus();

        }catch (Exception e){

            return ResponseEntity.badRequest().body(e.getMessage());
        }



        return ResponseEntity.ok(status);
    }

    // 페이징 처리 안된 모든 간호사 조회
    @Operation(summary = "간호사 전체 조회", description = "[페이징 처리 안됨]소속된 부서의 간호사들을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<?> getNurseList(){
        List<NurseStatusResponseDto> simpleNurseList = null;

        try {
            Member loginMember = authService.getLoginMember();
            Department department = loginMember.getDepartment();

            simpleNurseList = nurseService.getSimpleNurseList(department);

        }catch (Exception e){
            return ResponseEntity.badRequest().body(e);
        }


        return ResponseEntity.ok(simpleNurseList);
    }





    // 간호사들의 현재 상태 수정
    // 로그인한 간호사 계정의 과를 가져와서 이메일과 비밀번호로 수정 가능
    @Operation(summary = "간호사 현재 상태 수정", description = "간호사 계정으로 로그인한 해당 과의 간호사가 자신의 상태를 수정 가능")
    @PostMapping("/status")
    public ResponseEntity<?> updateNurseStatus(
            @RequestBody NurseStatusUpdateDto request) {
        NurseCurrentStatusDto nurseStatus = null;
        System.out.println(" nurseStatusUpdateDto : " + request.toString());
        try {
            nurseStatus = nurseService.updateNurseStatus(request);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());

        }
        return ResponseEntity.ok(nurseStatus);
    }

    @GetMapping("/get")
    public ResponseEntity<List<NurseNameEmailDto>> getNurseNames() {
        List<Nurse> nurses = nurseRepository.findAll();
        List<NurseNameEmailDto> result = nurses.stream()
                .map(n -> new NurseNameEmailDto(n.getId(), n.getName(), n.getEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }





}
