package com.gandalp.gandalp.hospital;

import com.gandalp.gandalp.hospital.domain.dto.ErCountUpdateDto;
import com.gandalp.gandalp.hospital.domain.dto.ErStatisticsRequestDto;
import com.gandalp.gandalp.hospital.domain.dto.ErStatisticsResponseDto;
import com.gandalp.gandalp.hospital.domain.dto.HospitalDto;
import com.gandalp.gandalp.hospital.domain.dto.HospitalErResponseDto;
import com.gandalp.gandalp.hospital.domain.entity.SortOption;
import com.gandalp.gandalp.hospital.domain.service.ErStatisticsService;
import com.gandalp.gandalp.hospital.domain.service.GeoCodingService;
import com.gandalp.gandalp.hospital.domain.service.HospitalService;
import com.gandalp.gandalp.member.domain.dto.NurseResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;
    private final GeoCodingService geoCodingService;
    private final ErStatisticsService erStatisticsService;
    private final SimpMessagingTemplate messagingTemplate;

    // 현재 위치에서 가까운 순으로 조회하려면  프론트에서 현재 위치를 보내줘야 함
    @Operation(summary = "응급실 병상 수용 정보 조회", description = "수용 가능한 병상 수 정보 거리 순으로 20개 조회")
    @PostMapping("/search")
    public ResponseEntity<?> getHospitals(
            @RequestParam double lat,
            @RequestParam double lon, // 현재 위치
            @RequestParam SortOption sortOption,
            @RequestParam(required = false) String keyword ){

//        geoCodingService.convertAllHospitalAddressToGeo();

        List<HospitalDto> hospitalList = null;

        try {
            hospitalList = hospitalService.getNearestHospitals(lon, lat, keyword, sortOption);

        }catch (Exception e ){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok(hospitalList);
    }

    // 병원 정보 단건 조회
    @Operation(summary = "로그인한 간호사의 병원 정보 조회", description = "간호사가 자신의 병원 정보를 조회할 수 있다.")
    @GetMapping("/data")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<?> getOneHospital(){

        HospitalDto responseDto = null;

        try{
             responseDto = hospitalService.getOneHospital();

        }catch (Exception e){

            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok(responseDto);
    }

    // 응급실 가용 병상 수 수정
    @Operation(summary = "응급실 가용 병상 수 수정", description = "응급실 가용 병상 수를 간호사가 직접 수정할 수 있다.")
    @PostMapping
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<?> updateErCount(@Valid @RequestBody ErCountUpdateDto dto) {

        HospitalErResponseDto resDto = null;

        try{
            // DB 업데이트
            resDto = hospitalService.updateErCount(dto);


            // websocket
            messagingTemplate.convertAndSend("/topic/er-status", resDto);

        }catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }


        return ResponseEntity.ok(resDto);
    }


    @Operation(summary = "응급실 병상 이용 시간대 분석", description = "응급실 병상 이용 시간대를 일, 월, 년 별로 조회할 수 있다.")
    @PostMapping("/inspect")
    public ResponseEntity<?> getErStatistics(
            @RequestBody ErStatisticsRequestDto requestDto
            ){

        List<ErStatisticsResponseDto> statistics = null;

        try{
            statistics = erStatisticsService.getStatistics(requestDto);

        }catch (Exception e){

            return ResponseEntity.badRequest().body(e.getMessage());
        }


        return ResponseEntity.ok(statistics);
    }




}
