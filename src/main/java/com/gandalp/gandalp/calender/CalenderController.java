package com.gandalp.gandalp.calender;

import com.gandalp.gandalp.calender.domain.dto.OrsGetRequestDto;
import com.gandalp.gandalp.calender.domain.dto.RoomResponseDto;
import com.gandalp.gandalp.calender.domain.service.CalenderService;
import com.gandalp.gandalp.calender.domain.dto.OrsDeleteRequestDto;
import com.gandalp.gandalp.calender.domain.dto.OrsRequestDto;
import com.gandalp.gandalp.calender.domain.dto.OrsResponseDto;
import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleDeleteRequestDto;
import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleRequestDto;
import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleResponseDto;
import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CalenderController {

    private final CalenderService calenderService;

    @Operation(summary = "간호사 개인 일정 생성", description = "간호사가 개인 일정을 생성합니다.")
    @PostMapping("/calendars")
    public ResponseEntity<PersonalScheduleResponseDto> createPersonalSchedule(@RequestBody PersonalScheduleRequestDto personalScheduleRequestDto) {

        PersonalScheduleResponseDto personalScheduleResponseDto = calenderService.createPersonalSchedule(personalScheduleRequestDto);

        return ResponseEntity.ok(personalScheduleResponseDto);
    }

    @Operation(summary = "간호사 개인 일정 삭제", description = "간호사가 개인 일정을 삭제합니다.")
    @DeleteMapping("/calendars")
    public ResponseEntity<Void> deletePersonalSchedule(@RequestBody PersonalScheduleDeleteRequestDto personalScheduleDeleteRequestDto) {

        calenderService.deletePersonalSchedule(personalScheduleDeleteRequestDto);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "간호사 개인 일정 수정", description = "간호사가 개인 일정을 수정합니다.")
    @PutMapping("/calendars")
    public ResponseEntity<PersonalScheduleResponseDto> updatePersonalSchedule(@RequestBody PersonalScheduleUpdateRequestDto personalScheduleUpdateRequestDto) {

        PersonalScheduleResponseDto personalScheduleResponseDto = calenderService.updatePersonalSchedule(personalScheduleUpdateRequestDto);

        return ResponseEntity.ok(personalScheduleResponseDto);
    }

    @Operation(summary = "간호사 개인 일정 조회", description = "간호사의 개인 일정을 조회합니다.")
    @GetMapping("/calendars/{nurse-id}")
    public ResponseEntity<List<PersonalScheduleResponseDto>> getPersonalSchedule(@PathVariable("nurse-id") Long nurseId) {
        List<PersonalScheduleResponseDto> personalScheduleResponseDtos = calenderService.getPersonalSchedules(nurseId);

        return ResponseEntity.ok(personalScheduleResponseDtos);
    }

    @Operation(summary = "모든 간호사 개인 일정 조회", description = "모든 간호사의 일정을 조회합니다.")
    @GetMapping("/calendars")
    public ResponseEntity<List<PersonalScheduleResponseDto>> getSchedules() {

        List<PersonalScheduleResponseDto> schedules = calenderService.getSchedules();

        return ResponseEntity.ok(schedules);
    }

    @Operation(summary = "수술실 예약", description = "간호사가 수술실을 예약합니다.")
    @PostMapping("/ors")
    public ResponseEntity<OrsResponseDto> createOrs(@RequestBody OrsRequestDto orsRequestDto) {

        OrsResponseDto orsResponseDto = calenderService.createOrs(orsRequestDto);

        return ResponseEntity.ok(orsResponseDto);
    }

    @Operation(summary = "수술실 예약 취소", description = "간호사가 수술실 예약을 취소합니다.")
    @DeleteMapping("/ors")
    public ResponseEntity<Void> deleteOrs(@RequestBody OrsDeleteRequestDto orsDeleteRequestDto) {

        calenderService.deleteOrs(orsDeleteRequestDto);

        return ResponseEntity.ok().build();
    }

//    @Operation(summary = "수술실 예약 조회", description = "입력한 날짜의 모든 수술실 예약 현황을 조회합니다.")
//    @GetMapping("/ors")
//    public ResponseEntity<OrsResponseDto> getOrs(OrsGetRequestDto orsGetRequestDto) {
//
//        // 내거 등록한 수술 예약 현황을 알아야 하는 것 아닌가?
//        // 수술실 예약 시 이름으로 들어가서 비밀번호 먼저 확인하는 식으로 해야 하나?
//        // 그럼 예약 신청 시 신청자 이름이랑 비밀번호 입력란 있어서 만들어야 할 듯. 근데 아래꺼보단 이걸로해야 예약취소버튼 등이 있어서 이걸로 로그인 형식으로 해야 할듯
//        // 간호사 아이디로 포함되어 있는 일정은 취소할 수 있는 형식으로 예약 최소 이렇게 표현하기
//        // 내가 포함된 수술실만 조회하는 것도 해야 하나? 이건 필터링 있어야 하나?
//
//        return ResponseEntity.ok().build();
//
//    }

    // 수술실에 들어간 간호사의 근무 상태를 바꿔야함 -> 이거 어떻게 하지??

    @Operation(summary = "수술실 예약 조회", description = "수술실의 모든 예약 현황을 조회합니다.")
    @GetMapping("/ors")
    public ResponseEntity<List<OrsResponseDto>> getOrs() {

        List<OrsResponseDto> orsResponseDtos = calenderService.getOrs();

        return ResponseEntity.ok(orsResponseDtos);
    }

    @Operation(summary = "수술실 목록 조회", description = "모든 수술실을 조회합니다.")
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomResponseDto>> getRooms() {

        List<RoomResponseDto> roomResponseDtos = calenderService.getRooms();

        return ResponseEntity.ok(roomResponseDtos);
    }

}
