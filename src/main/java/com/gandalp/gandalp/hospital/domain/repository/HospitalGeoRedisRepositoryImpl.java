package com.gandalp.gandalp.hospital.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HospitalGeoRedisRepositoryImpl implements HospitalGeoRedisRepository {

    // direction으로 도로 기준으로 가까운 응급실 아이디 20곳 조회


    private final RedisTemplate<String, String> redisTemplate;

    private static final String GEO_KEY = "hospital:geo";

    // 좌표로 바꾼 주소를 redis에 저장
    @Override
    public void saveHospitalLocation(Long hospitalId, double longitude, double latitude) {
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(longitude, latitude), hospitalId.toString());
    }

    // redis에서 가까운 병원 id 후보 50개 조회
    @Override
    public List<Long> findNearbyHospitalIds(double longitude, double latitude, int count){
        log.info("▶ findNearbyHospitalIds 호출: 위도={}, 경도={}, limit={}", latitude, longitude, count);

        // 1. 반경을 조금씩 늘려서 조회

        double minRadius = 20; // 20km
        double maxRadius = 100;
        double step = 1;

        double radius = minRadius;
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> results;

        // 2. results.size()가 50이 될 때까지 조회
        do{
            results = redisTemplate.opsForGeo()
                    .radius(
                            "hospital:geo",
                            new Circle(new Point(longitude, latitude), new Distance(radius, Metrics.KILOMETERS)),
                            GeoRadiusCommandArgs.newGeoRadiusArgs()
                                    .includeDistance()
                                    .sortAscending()
                                    .limit(50)
                    )
                    .getContent();
            if(results.size() >= 50) {
                break;
            }
            // 반경은 최소부터 1km 씩 넓어짐( 최대 반경 전까지 )
            radius = Math.min(radius + step, maxRadius);
        }while(radius < maxRadius);


        // 4. id 리스트로 변환
        List<Long> hospitalIdList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {

            String hs = result.getContent().getName();
            Long id = Long.valueOf(hs);
            hospitalIdList.add(id);
        }

        return hospitalIdList;

    }



    // 좌표 돌려주는 메서드
    @Override
    public List<Point> findLocationsByIds(List<Long> hospitalIds) {
        if (hospitalIds == null || hospitalIds.isEmpty()) {
            return Collections.emptyList();
        }
        // RedisGeoOperations.position()은 입력한 순서대로 Point 리스트를 돌려줍니다.
        String[] members = hospitalIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
        List<Point> points = redisTemplate.opsForGeo()
                .position(GEO_KEY, members);
        // (null이 올 수도 있으니, 필요하다면 필터링 or 예외 처리)
        return points;
    }
}
