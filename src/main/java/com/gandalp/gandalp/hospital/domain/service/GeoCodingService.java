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

        // 위/경도가 없는 병원은 네이버 API 가져와서 DB와 Redis에 모두 저장
        List<Hospital> missingGeo = hospitalRepository.findByLatitudeIsNullOrLongitudeIsNull();
        for (Hospital hospital : missingGeo) {
            GeoResponse geo = naverGeoClient.getGeoPointFromAddress(hospital.getAddress());
            if (geo != null) {
                hospital.updateGeoCode(geo.getLatitude(), geo.getLongitude());
                hospitalGeoRedisRepository.saveHospitalLocation(
                        hospital.getId(),
                        geo.getLongitude(),
                        geo.getLatitude()
                );
            }
        }
        System.out.println("GeoCodingService: 신규 변환 " + missingGeo.size() + "건 DB와 Redis에 저장");

        // 이미 DB에 위/경도가 있는데 redis에 없는 경우 
        List<Hospital> allWithGeo = hospitalRepository.findAll();
        int count = 0;
        for (Hospital h : allWithGeo) {
            if (h.getLatitude() != null && h.getLongitude() != null) {
                // redis에만 올림 (db에는 이미 있음)
                hospitalGeoRedisRepository.saveHospitalLocation(
                        h.getId(),
                        h.getLongitude(),
                        h.getLatitude()
                );
                count++;
            }
        }
        System.out.println("GeoCodingService: Redis에 위/경도 " + count + "건 저장");
    }
}

