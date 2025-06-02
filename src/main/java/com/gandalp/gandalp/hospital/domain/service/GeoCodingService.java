package com.gandalp.gandalp.hospital.domain.service;

import com.gandalp.gandalp.hospital.domain.dto.GeoResponse;
import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.gandalp.gandalp.hospital.domain.repository.HospitalGeoRedisRepository;
import com.gandalp.gandalp.hospital.domain.repository.HospitalRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeoCodingService {

    private final HospitalRepository hospitalRepository;
    private final HospitalGeoRedisRepository hospitalGeoRedisRepository;
    private final NaverGeoClient naverGeoClient;


    //DB에 저장된 병원 주소를 위도/경도로 변환 ( 앱 실행, 병원 정보가 수정되는 경우에만 실행)
    @Transactional
    public void convertAllHospitalAddressToGeo(){

        List<Hospital> hospitals =
                hospitalRepository.findByLatitudeIsNullOrLongitudeIsNull();


        for(Hospital hospital: hospitals){


            GeoResponse geo = naverGeoClient.getGeoPointFromAddress(hospital.getAddress());

            if (geo != null) {
                // local db에 저장
                hospital.updateGeoCode(geo.getLatitude(), geo.getLongitude());

                // redis에 저장
                // Redis 저장을 hospitalGeoRedisRepository에서 하게 함
                hospitalGeoRedisRepository.saveHospitalLocation(
                        hospital.getId(),
                        geo.getLongitude(),
                        geo.getLatitude()
                );

            }
        }

        System.out.println(
                "GeoCodingService: 신규 병원 " + hospitals.size() +" 건 좌표 DB/Redis 저장 완료");
    }
        // 위는 처음에 db에 좌표가 없는 경우 조회해서 넣는거고 아래는 좌표가 있는 애들을 redis에 저장
//        List<Hospital> allHospitals = hospitalRepository.findAll();
//
//        for (Hospital h : allHospitals) {
//            // 이미 DB에 위/경도 정보가 있다고 가정 → 바로 Redis에 저장
//            // (만약 위/경도 값이 null일 가능성이 있다면, null 체크 후 Geocoding 로직을 추가하세요)
//            if (h.getLatitude() != null && h.getLongitude() != null) {
//                hospitalGeoRedisRepository.saveHospitalLocation(
//                        h.getId(),
//                        h.getLongitude(),
//                        h.getLatitude()
//                );
//            }
//        }
//
//        System.out.println("GeoCodingService: Redis-Geo에 " + allHospitals.size() + "건 로드 완료");
//    }
}
