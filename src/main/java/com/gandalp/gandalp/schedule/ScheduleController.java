package com.gandalp.gandalp.schedule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.gandalp.gandalp.schedule.domain.dto.*;
import com.gandalp.gandalp.schedule.domain.entity.Schedule;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.gandalp.gandalp.member.domain.dto.NurseResponseDto;
import com.gandalp.gandalp.member.domain.entity.Status;
import com.gandalp.gandalp.schedule.domain.entity.SelectOption;
import com.gandalp.gandalp.schedule.domain.service.NurseStaticsService;
import com.gandalp.gandalp.schedule.domain.service.ScheduleService;
import com.gandalp.gandalp.schedule.domain.service.SurgeryScheduleService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final SurgeryScheduleService surgeryScheduleService;
    private final NurseStaticsService nurseStaticsService;
    private final ScheduleRepository scheduleRepository;

    @Operation(summary = "오프 생성")
    @PostMapping("/off")
    public ResponseEntity<?> createOffSchedule(@RequestBody OffScheduleRequestDto scheduleRequestDto) {
        OffScheduleTempResponseDto offScheduleTempResponseDto = null;
        try {
            offScheduleTempResponseDto = scheduleService.createOffSchecule(scheduleRequestDto);
        }
        catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return offScheduleTempResponseDto == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(offScheduleTempResponseDto);
    }

    @Operation(summary = "임시 오프 삭제")
    @DeleteMapping("/off/temp/{schedule-temp-id}")
    public ResponseEntity<?> deleteOffSchedule(@PathVariable("schedule-temp-id") Long scheduleTempId,
                                                @RequestParam String email) {
        try {
            ScheduleResponseDto offScheduleResponseDto = scheduleService.deleteOffScheduleTemp(scheduleTempId,email);
            return ResponseEntity.ok().body(offScheduleResponseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "전체 오프 조회")
    @GetMapping("/off/temp")
    public ResponseEntity<?> getAllOffScheduleTemp(
            @PageableDefault(size = 10, page = 0, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        System.out.println("전체 오프 조회");
        try {
            Page<OffScheduleTempResponseDto> list = scheduleService.getAllOffScheduleTemp(pageable);

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/off/temp/nurse")
    public ResponseEntity<?> getOffSchedule(String email,
                                            @PageableDefault(size = 10, page = 0, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<OffScheduleTempResponseDto> offScheduleTempResponseDtos = null;
        try {
            offScheduleTempResponseDtos = scheduleService.getOffScheduleTemp(email, pageable);

            return ResponseEntity.ok().body(offScheduleTempResponseDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/off/temp/nurse/name")
    public ResponseEntity<?> getOffScheduleByName(String name, @PageableDefault(size = 10, page = 0, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<OffScheduleTempResponseDto> offScheduleTempResponseDtos = null;
        try {
            offScheduleTempResponseDtos = scheduleService.getOffScheduleTempByName(name, pageable);
            return ResponseEntity.ok().body(offScheduleTempResponseDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    // 승인 대기 중인 오프 관리

    // 임시 스케줄에 있는 오프 승인 시 -> 승인됨으로 바뀌고 스케쥴에 카테고리 승인된 오프로 바뀌고 삽입
    @Operation(summary = "오프 승인")
    @PostMapping("/acceptOff/{schedule-temp-id}")
    @PreAuthorize("hasRole('HEAD_NURSE')")
    public ResponseEntity<?> acceptOffSchedule(@PathVariable("schedule-temp-id") Long scheduleTempId) {
        try {
            ScheduleResponseDto offScheduleResponseDto = scheduleService.acceptOff(scheduleTempId);
            return ResponseEntity.ok().body(offScheduleResponseDto);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // 임시 스케줄에 있는 오프 반려 시 -> 반려됨으로 바뀜
    @Operation(summary = "오프 반려")
    @PutMapping("/rejectOff/{schedule-temp-id}")
    @PreAuthorize("hasRole('HEAD_NURSE')")
    public ResponseEntity<?> rejectOffSchedule(@PathVariable("schedule-temp-id") Long scheduleTempId) {
        try {
            ScheduleResponseDto offScheduleResponseDto = scheduleService.rejectOff(scheduleTempId);
            return ResponseEntity.ok().body(offScheduleResponseDto);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이메일과 비밀번호 체크
    @Operation(summary = "이메일과 비밀번호 체크")
    @PostMapping("/check")
    public ResponseEntity<?> checkPassword(String password, String email) {
        try {
            NurseResponseDto nurseResponseDto = scheduleService.checkPassword(password, email);
            return ResponseEntity.ok().body(nurseResponseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


    // 수술 일정 조회
    @Operation(summary = "수술 일정 조회", description = "수간호사와 간호사가 수술 일정을 조회 가능")
    @GetMapping("/surgery")
    public ResponseEntity<?> getAllSurgerySchedule(){

        List<SurgeryScheduleResponseDto> surgerySchedules = null;

        try{
             surgerySchedules = surgeryScheduleService.getAllSurgerySchedule();


        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }


        return ResponseEntity.ok(surgerySchedules);
    }


    // 간호사 근무 분석
    // 오프 미 포함 근무, 수술 통계 조회
    // 아무것도 선택하지 않으면 이전 달의 근무 분석표가 보임
    // 옵션, 근무 상태( 기본 전체 다 보여줌 ) - 간호사별 업무 비율에서는 다 보여주고 간호사별 근무량 비교에서는 status를 선택으로 받아서
    @Operation(
        summary = "간호사 통합 통계 조회",
        description = "과에 소속된 간호사들의 근무/오프/수술 통계를 통합 조회합니다. status 값으로 필터링할 수 있습니다."
    )
    @PostMapping("/status/working")
    public ResponseEntity<?> getAllNurseWorking(@Valid @RequestBody StaticRequestDto staticRequestDto){

        List<StaticsResponseDto> workingStatics = null;

        try{
            workingStatics = nurseStaticsService.getWorkingStatics(staticRequestDto);

        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok(workingStatics);
    }


    // 승인된 오프 삭제
    @Operation(summary = "승인된 오프 삭제")
    @DeleteMapping("/off/{schedule-id}")
    public ResponseEntity<?> deleteOff(@PathVariable("schedule-id") Long scheduleId){
        try {
            ScheduleResponseDto offScheduleResponseDto = scheduleService.deleteOff(scheduleId);
            return ResponseEntity.ok().body(offScheduleResponseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 승인된 오프 조회
    @Operation(summary = "승인된 오프 조회")
    @GetMapping("/off")
    public ResponseEntity<?> getOffScheduleByEmail(String email) {
        try {
            List<ScheduleResponseDto> offScheduleResponseDtos = scheduleService.getOffSchedules(email);
            return ResponseEntity.ok().body(offScheduleResponseDtos);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    //임시 근무 조회
    @Operation(summary = "임시 근무 조회")
    @GetMapping("/temp")
    @PreAuthorize("hasRole('HEAD_NURSE')")
    public ResponseEntity<?> showAllWorkTemp() {
        try {
            List<WorkTempResponseDto> workTempResponseDtos = scheduleService.getAllOffWorkTemp();
            return ResponseEntity.ok().body(workTempResponseDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "임시 근무 수정")
    @PutMapping("/temp")
    @PreAuthorize("hasRole('HEAD_NURSE')")
    public ResponseEntity<?> updateWorkTemp(@RequestBody WorkTempRequestUpdateDto workTempRequestUpdateDto){
        try {
            WorkTempResponseDto workTempResponseDto = scheduleService.updateWorkTemp(workTempRequestUpdateDto);
            return ResponseEntity.ok().body(workTempResponseDto);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Operation(summary = "임시 근무 삭제")
    @DeleteMapping("/temp/{work-temp-id}")
    @PreAuthorize("hasRole('HEAD_NURSE')")
    public ResponseEntity<?> deleteWorkTemp(@PathVariable("work-temp-id") Long workTempId) {
        try {
            scheduleService.deleteWorkTemp(workTempId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "근무 조회")
    @GetMapping
    public ResponseEntity<?> showWork() {
        try {
            List<WorkScheduleResponseDto> schedules = scheduleService.showWork();
            return ResponseEntity.ok().body(schedules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "근무 승인")
    @PostMapping("/accept-work/{work-schedule-id}")
    public ResponseEntity<?> acceptWork(@PathVariable("work-schedule-id") Long workScheduleId) {
        try {
            ScheduleResponseDto workScheduleResponseDto = scheduleService.acceptWork(workScheduleId);

            return ResponseEntity.ok().body(workScheduleResponseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "근무 승인")
    @PostMapping("/accept-works")
    public ResponseEntity<?> acceptWorks(@RequestBody List<Long> workScheduleIds) {
        try {
            List<ScheduleResponseDto> workScheduleResponseDtos = new ArrayList<>();
            for(Long id : workScheduleIds) {
                workScheduleResponseDtos.add(scheduleService.acceptWork(id));
            }


            return ResponseEntity.ok().body(workScheduleResponseDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsSchedule(
            @RequestParam Long nurseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime
    ) {
        // 데이: 6~14, 이브닝: 14~22, 나이트: 22~06
        // startTime이 포함되는 일정이 있는지 확인
        boolean exists = scheduleRepository.existsByNurseIdAndStartTimeLessThanEqualAndEndTimeGreaterThan(
                nurseId, startTime, startTime
        );
        return ResponseEntity.ok(exists);
    }


}
