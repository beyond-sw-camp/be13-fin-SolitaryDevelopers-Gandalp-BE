package com.gandalp.gandalp.hospital;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.hospital.domain.dto.HospitalDto;
import com.gandalp.gandalp.hospital.domain.entity.SortOption;
import com.gandalp.gandalp.hospital.domain.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class LocationStompController {

    private final HospitalService hospitalService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthService authService;

    @MessageMapping("/location")
    public void onLocation(@Header(value = "Authorization", required = false) String bearerToken, LocationMessage msg, Principal principal) {

        // 1) 계정 식별자 결정: 헤더 토큰 우선, 없으면 principal
        String accountId;
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            var member = authService.loadMemberByToken(token);
            accountId = member.getAccountId();
        } else {
            accountId = principal.getName();
        }

        List<HospitalDto> list = hospitalService.getNearestHospitals(
                msg.getLon(), // 경도
                msg.getLat(), // 위도
                null, //키워드 ( 현재 위치라 검색어가 없음
                SortOption.DISTANCE // 기본 거리순
        );

        messagingTemplate.convertAndSendToUser(
                accountId,
                "/queue/near-hospitals",
                list
        );
    }


    // STOMP가 자동으로 매핑해주는 DTO
    public static class LocationMessage {
        private double lat;
        private double lon;
        private double getLat() {return lat;}
        private void setLat(double lat) {this.lat = lat;}
        private double getLon() {return lon;}
        private void setLon(double lon) {this.lon = lon;}
    }


}
