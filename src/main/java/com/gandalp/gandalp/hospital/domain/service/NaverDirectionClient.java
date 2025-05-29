package com.gandalp.gandalp.hospital.domain.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gandalp.gandalp.hospital.domain.dto.RouteInfoDto;
import org.springframework.stereotype.Service;

import com.gandalp.gandalp.hospital.domain.dto.DestinationDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverDirectionClient {

    private final DirectionCacheService cacheService;

    /**
     * 현재 위치로부터 목적지 병원들까지의 도로 거리(km)를 계산한다.
     * 실패 시 해당 병원은 결과에서 제외된다.
     *
     * @param latitude     현재 위치 위도
     * @param longitude    현재 위치 경도
     * @param destinations 병원 리스트 (병원 ID + 위도/경도 정보 포함)
     * @return 병원 ID → 거리(km) 매핑
     */
    public Map<Long, RouteInfoDto> getRoadDistances(
        double longitude, double latitude,
        List<DestinationDto> destinations
    ) {
        log.info("🚗 도로 거리 계산 시작 - 기준점: ({}, {}), 대상 병원 수: {}", latitude, longitude, destinations.size());

        Map<Long, RouteInfoDto> routes = new HashMap<>();
        final int BATCH = 25;

        for (int i = 0; i < destinations.size(); i += BATCH) {
            int end = Math.min(i + BATCH, destinations.size());
            List<DestinationDto> batch = destinations.subList(i, end);

            for (DestinationDto dest : batch) {
                try {
                    RouteInfoDto info = cacheService.getDistanceForPair(longitude, latitude, dest);

                    if (info.getDistanceKm() != Double.MAX_VALUE) { // 값이 있으면
                        routes.put(dest.getHospitalId(), info);
                        log.debug(" 거리 계산 성공 - 병원 ID: {}, 거리: {}km, 시간 : {}sec", dest.getHospitalId(), info.getDistanceKm()
                        , info.getDurationSec());
                    } else {
                        log.warn("❌ 거리 계산 실패 (Double.MAX_VALUE) - 병원 ID: {}, 좌표: ({}, {})",
                            dest.getHospitalId(), info.getDistanceKm(), info.getDurationSec());
                    }

                } catch (Exception e) {
                    log.error(" 거리 계산 중 예외 발생 - 병원 ID: {}, 좌표: ({}, {})",
                        dest.getHospitalId(), dest.getLatitude(), dest.getLongitude(), e);
                }
            }
        }

        log.info(" 도로 거리 계산 완료 - 성공 병원 수: {}", routes.size());
        return routes;
    }
}
