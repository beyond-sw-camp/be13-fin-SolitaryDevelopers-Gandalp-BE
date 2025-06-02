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

    // redis의 geo 서비스 이용해서 도로상이 아니라 좌표 기준으로 가까운 50곳 조회


    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GEO_KEY = "hospital:geo";

    // 좌표로 바꾼 주소를 redis에 저장
    @Override
    public void saveHospitalLocation(Long hospitalId, double longitude, double latitude) {
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(longitude, latitude), hospitalId.toString());
    }

    // redis에서 가까운 병원 id 후보 50개 조회
    @Override
    public List<Long> findNearbyHospitalIds(double longitude, double latitude, int count){

        // 디버그용: 지금 주입된 redisTemplate의 keySerializer 확인
        log.debug("▶ 확인: redisTemplate.getKeySerializer() = {}", redisTemplate.getKeySerializer().getClass().getSimpleName());


        log.info("▶ findNearbyHospitalIds 호출: 위도={}, 경도={}, limit={}", latitude, longitude, count);

        // 1. 반경을 조금씩 늘려서 조회

        double minRadius = 20; // 20km
        double maxRadius = 100;
        double step = 1;

        double radius = minRadius;
        List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> results;

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
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results) {

            String hs = result.getContent().getName().toString();
            Long id = Long.valueOf(hs);
            hospitalIdList.add(id);
        }

        return hospitalIdList;

    }

    //members는 위에서 찾은 50개의 병원 id를 string 배열로 저장하여
    // redis의 geo key hospital:geo에서 조회 시 병원 id를 member name으로 사용해서 조회

    // points는 members 배열에 담긴 문자열을 순서대로 조회해서 redis에 저장된 좌표 정보를
    // points 형태로 반환받아 리스트로 돌려줌

    // 위에서 redis에서 조회한 상위 50개 병원 id를 string 배열에 담아서 다시 좌표로 조회할 때 사용


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
        // position(key, Object... members) 시그니처에 맞춰 Object[]로 캐스팅
        @SuppressWarnings("unchecked")
        List<Point> points = (List<Point>) (List<?>)redisTemplate.opsForGeo()
                .position(GEO_KEY, (Object[])members);
        // (null이 올 수도 있으니, 필요하다면 필터링 or 예외 처리)
        return points;
    }
}
