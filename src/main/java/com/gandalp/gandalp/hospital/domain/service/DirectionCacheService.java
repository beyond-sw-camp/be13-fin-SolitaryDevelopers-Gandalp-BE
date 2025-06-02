package com.gandalp.gandalp.hospital.domain.service;

import com.gandalp.gandalp.hospital.domain.dto.DestinationDto;
import com.gandalp.gandalp.hospital.domain.dto.RouteInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DirectionCacheService {

    // cacheable은 같은 클래스 내부에서 자기 메서드 호출 시 동작하지 않아서
    // 요청은 별도 빈으로 분리

    @Value("${naver.map.client-id}")
    private String clientId;

    @Value("${naver.map.client-secret}")
    private String clientSecret;

    private static final String OPTION  = "trafast";
    // trafast 실시간 빠른 길

    // 도로상 실제 거리 가져오기
    // 캐시 적용
    // 한번 계산한 origin + destination 쌍을 redis에 저장해 해당 구간은 네이버 api 호출하지 않도록 함

    /*
     * 목적지 리스트(50개)를 최대 25개씩 나눠서
     * 캐시에서 거리를 꺼냄(있으면 재사용)
     * 없으면 네이버 api 호출하고 캐시에 저장
     * 병원과 현재 위치에서의 거리를 돌려줌
     * */

    // 소수점이 너무 길어서 미세하게 다른 경우 계속 API 호출하는 문제 반올림해서 4~50 정도는 커버해서
    // naver api  호출 횟수를 줄임


    // 거리 계산만 담당 ( redis에 저장 )
    @Cacheable( cacheNames = "routeDistances",
            key = "T(com.gandalp.gandalp.hospital.domain.service.DirectionCacheService)" +
                    ".makeCacheKey(#longitude, #latitude, #dest.longitude, #dest.latitude)"
    )
    public RouteInfoDto getDistanceForPair(double longitude, double latitude, DestinationDto dest) {

        // 전달받은 좌표를 반올림 (소수점 6자리)
        // double은 타입을 소수점 자릿수를 정확하게 반올림하기 어려줘 캐시 키로 쓰면 일관성이 깨질 수 있음
        // BigDecimal은 10진법으로 정확하게 소수점 반올림 관리 가능
        BigDecimal lonBd = BigDecimal.valueOf(longitude).setScale(5, RoundingMode.HALF_UP);
        BigDecimal latBd = BigDecimal.valueOf(latitude).setScale(5, RoundingMode.HALF_UP);
        BigDecimal destLonBd = BigDecimal.valueOf(dest.getLongitude()).setScale(6, RoundingMode.HALF_UP);
        BigDecimal destLatBd = BigDecimal.valueOf(dest.getLatitude()).setScale(6, RoundingMode.HALF_UP);


        // 반올림된 좌표를 문자열로 바꿈
        String lonRoundedStr      = lonBd.toPlainString();
        String latRoundedStr      = latBd.toPlainString();
        String destLonRoundedStr  = destLonBd.toPlainString();
        String destLatRoundedStr  = destLatBd.toPlainString();


        // redis 캐시에 없으면 로그 찍힘
        log.info("▶ [캐시 미스] origin={}→dest={}", longitude + "," + latitude,
                dest.getLongitude() + "," + dest.getLatitude());
        // redis 캐시에만 반올림된 값을 저장하는게 아니라 요청 보낼 때도 반올림한 값으로 보내야함
//        String start = longitude + "," + latitude;
//        String goal  = dest.getLongitude() + "," + dest.getLatitude();

        // 반올림된 좌표로 api 호출
        String start = lonRoundedStr + "," + latRoundedStr;
        String goal = destLonRoundedStr + "," + destLatRoundedStr;


        String url   = "https://maps.apigw.ntruss.com/map-direction-15/v1/driving"
                + "?start=" + URLEncoder.encode(start, StandardCharsets.UTF_8)
                + "&goal="  + URLEncoder.encode(goal,  StandardCharsets.UTF_8)
                + "&option=" + OPTION
                + "&cartype=" + 1;
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            conn.setRequestProperty("X-NCP-APIGW-API-KEY",    clientSecret);

            if(conn.getResponseCode() != 200){
                conn.disconnect();
                return RouteInfoDto.empty();
            }


            StringBuilder sb = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) sb.append(line);
            }
            conn.disconnect();

            JSONObject root     = new JSONObject(sb.toString());
            JSONObject routeObj = root.getJSONObject("route");
            JSONArray arr      = routeObj.getJSONArray(OPTION);
            if (arr.isEmpty()) return RouteInfoDto.empty();

            JSONObject summary = arr.getJSONObject(0).getJSONObject("summary");
            double distanceKm = summary.getDouble("distance") / 1000.0;
            double durationSec = summary.getDouble("duration");

            return new RouteInfoDto(distanceKm, durationSec);

        } catch (Exception ex) {
            log.error("Direction API 예외: dest={}", dest, ex);
            return RouteInfoDto.empty();
        }


    }

    // redis 키 생성

    // 위에 표현식에서 사용함
    public static String makeCacheKey(double lon, double lat, double destLon, double destLat) {

        BigDecimal lonBd = BigDecimal.valueOf(lon).setScale(5, RoundingMode.HALF_UP);
        BigDecimal latBd = BigDecimal.valueOf(lat).setScale(5, RoundingMode.HALF_UP);
        BigDecimal destLonBd = BigDecimal.valueOf(destLon).setScale(5, RoundingMode.HALF_UP);
        BigDecimal destLatBd = BigDecimal.valueOf(destLat).setScale(5, RoundingMode.HALF_UP);
        return lonBd + "," + latBd + ":" + destLonBd + "," + destLatBd;
    }


}
