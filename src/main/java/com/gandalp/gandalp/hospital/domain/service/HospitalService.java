package com.gandalp.gandalp.hospital.domain.service;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.hospital.domain.dto.DepartmentDto;
import com.gandalp.gandalp.hospital.domain.dto.DestinationDto;
import com.gandalp.gandalp.hospital.domain.dto.ErCountUpdateDto;
import com.gandalp.gandalp.hospital.domain.dto.GeoResponse;
import com.gandalp.gandalp.hospital.domain.dto.HospitalDto;
import com.gandalp.gandalp.hospital.domain.dto.HospitalErResponseDto;
import com.gandalp.gandalp.hospital.domain.dto.RouteInfoDto;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.hospital.domain.entity.ErStatistics;
import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.gandalp.gandalp.hospital.domain.entity.SortOption;
import com.gandalp.gandalp.hospital.domain.repository.DepartmentRepository;
import com.gandalp.gandalp.hospital.domain.repository.ErStatisticsRepository;
import com.gandalp.gandalp.hospital.domain.repository.HospitalGeoRedisRepository;
import com.gandalp.gandalp.hospital.domain.repository.HospitalRepository;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.RouteMatcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Map.Entry.comparingByValue;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalGeoRedisRepository hospitalGeoRedisRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NaverGeoClient naverGeoClient;
    private final NaverDirectionClient naverDirectionClient;
    private final ErStatisticsRepository erStatisticsRepository;

    private static final String GEO_KEY = "hospital:geo";
    private final ErStatisticsService erStatisticsService;
    private final AuthService authService;
    private final DepartmentRepository departmentRepository;

    // 병원 단 건 조회
    public HospitalDto getOneHospital( ) {

        // 1. 로그인 했는지 검증
        Member member = authService.getLoginMember();

        // 2. 로그인 한 사람의 병원 가져오기
        Long hospitalId = member.getHospital().getId();

        Hospital hospital = hospitalRepository.findById(hospitalId).orElseThrow(
                () -> new EntityNotFoundException("해당하는 병원이 존재하지 않습니다.")
        );


        return new HospitalDto(hospital);
    }

    @Transactional
    public HospitalErResponseDto updateErCount(ErCountUpdateDto updateDto) {

        // 1. 로그인 했는지 검증
        Member member = authService.getLoginMember();

        // 2. 로그인 한 사람의 병원 가져오기
        Hospital hospital = member.getHospital();

//        Hospital hospital = hospitalRepository.findById(updateDto.getHospitalId()).orElseThrow(
//                () -> new EntityNotFoundException("해당 병원은 존재하지 않습니다.")
//        );
//

        int nowErCount = hospital.updateAvailableErCount(updateDto.getAvailableErCount());


        LocalDateTime now = LocalDateTime.now();
        ErStatistics statistics = ErStatistics.builder()
                                            .hospital(hospital)
                                            .year(now.getYear())
                                            .month(now.getMonthValue())
                                            .day(now.getDayOfMonth())
                                            .hour(now.getHour())
                                            .patients(nowErCount)
                                            .build();

        erStatisticsRepository.save(statistics);






        return new HospitalErResponseDto(hospital);
    }


    // 병원 좌표 갱신 ( 최초 1회 또는 주소 수정 시 ) 
    // - geocoding 서비스에 있고 application 실행시 자동으로 좌표 없는 애들 변환해서 넣게 함
    
//    public void updateGeoPointFroAllHospitals() {
//        List<Hospital> hospitals = hospitalRepository.findAll();
//        for (Hospital hospital : hospitals) {
//            GeoResponse geo = naverGeoClient.getGeoPointFromAddress((hospital.getAddress()));
//
//            hospitalGeoRedisRepository.saveHospitalLocation(
//                    hospital.getId(),
//                    geo.getLongitude(),
//                    geo.getLatitude()
//            );
//        }
//    }

    // 검색이 없으면 기본 현재 위치에서 가까운 순으로 응급실 20곳 조회
    // 거리순, 가용 병상 순
    // 검색을 하는 경우 주소, 병원 이름
    public List<HospitalDto> getNearestHospitals(double longitude, double latitude, String keyword, SortOption sortOption ) {

        System.out.println("long "+ longitude+ " lan = "+ latitude);

            // 1) Redis Geo에서 반경 제한 없이 최단거리 후보 ID(최대 50개 정도 여유 있게) 조회
            List<Long> candidateIds = hospitalGeoRedisRepository.findNearbyHospitalIds(longitude, latitude, 50);
            System.out.println("redis list : "+ candidateIds);
            // 만약 후보가 없다면 빈 페이지 반환
            if (candidateIds.isEmpty()) {
                throw new EntityNotFoundException("주변 병원이 없습니다.");
            }


            // 위에서 후보로 조회한 각 병원 ID의 위·경도 정보를 조회
            List<Point> points = hospitalGeoRedisRepository.findLocationsByIds(candidateIds);

            System.out.println("points : "+ points);

            // 3) Point 리스트를 네이버 Direction API용 DTO로 변환
            List<DestinationDto> destinations = IntStream.range(0, candidateIds.size())
                    .mapToObj(i -> new DestinationDto(
                            candidateIds.get(i),
                            points.get(i).getY(),  // Point(x=lon, y=lat)
                            points.get(i).getX()
                    ))
                    .collect(Collectors.toList());
            // 반환 예시: { hospitalId1 → 1.2km, hospitalId2 → 0.9km, … }
            System.out.println("destinations : "+ destinations);

            // 4) 네이버 Direction API 호출 (service=15, batch size 최대 25)
            Map<Long, RouteInfoDto> roadDistances = naverDirectionClient.getRoadDistances(
                    longitude, latitude,
                    destinations
            );

            System.out.println("roadDistances : "+ roadDistances.toString());


            // 5) 거리 순 정렬 후 상위 20개 ID 뽑기
            List<Long> top20Ids = roadDistances.entrySet().stream()
                    .sorted(comparingByValue(Comparator.comparingDouble(RouteInfoDto::getDistanceKm)))
                    .limit(20)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            System.out.println(top20Ids);
            // 6) 거리/검색/정렬 조건을 적용해 JPA로 조회

        IntStream.range(0, top20Ids.size()).forEach(i -> {
            Long id = top20Ids.get(i);
            double dist = roadDistances.get(id).getDistanceKm();
            log.debug("순위 {} → 병원ID: {}, 거리: {}km", i+1, id, dist);
        });

            List<HospitalDto> list = hospitalRepository.searchNearbyHospitals(top20Ids, keyword, sortOption);


            // 7 ) 조회한 병원 List에 거리, 시간 넣어줌

        for(HospitalDto h : list) {
            RouteInfoDto info = roadDistances.getOrDefault(h.getId(), RouteInfoDto.empty());
            h.setDistanceKm(info.getDistanceKm());
            h.setDurationSec(info.getDurationSec());
        }


        return list;
    }

    public List<DepartmentDto> getDepartments() {
        Member loginMember = authService.getLoginMember();

        Hospital hospital = loginMember.getHospital();

        List<Department> list = departmentRepository.findByHospital(hospital);

        return list.stream()
            .map( department -> DepartmentDto.builder()
                .id(department.getId())
                .hospitalName(hospital.getName())
                .departmentName(department.getName())
                .build()
            )
            .collect(Collectors.toList());
    }



}
