package com.gandalp.gandalp.hospital.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.RouteMatcher;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteInfoDto implements Serializable {

    // 전체 경로 거리랑 소요 시간

    private double distanceKm;

    private double durationSec;


    // 실패 시 빈 객체 반환
    public static RouteInfoDto empty() {
        return new RouteInfoDto(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
