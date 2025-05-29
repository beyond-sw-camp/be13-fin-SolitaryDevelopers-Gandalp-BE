package com.gandalp.gandalp.hospital.domain.dto;

import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDto {

    private Long   id;
    private String name;
    private String address;
    private String phoneNumber;
    private int totalErCount;
    private int availableErCount;
    private double latitude;
    private double longitude;

    private double distanceKm;
    private double durationSec;

    // querydsl 용

    public HospitalDto(
            Long   id,
            String name,
            String address,
            String phoneNumber,
            int    totalErCount,
            int    availableErCount,
            double latitude,
            double longitude
    ) {
        this.id               = id;
        this.name             = name;
        this.address          = address;
        this.phoneNumber      = phoneNumber;
        this.totalErCount     = totalErCount;
        this.availableErCount = availableErCount;
        this.latitude         = latitude;
        this.longitude        = longitude;
    }
    public HospitalDto(Hospital hospital) {
        this.id               = hospital.getId();
        this.name             = hospital.getName();
        this.address          = hospital.getAddress();
        this.phoneNumber      = hospital.getPhoneNumber();
        this.totalErCount     = hospital.getTotalErCount();
        this.availableErCount = hospital.getAvailableErCount();
        this.latitude = hospital.getLatitude() != null ? hospital.getLatitude() : 0.0;
        this.longitude = hospital.getLongitude() != null ? hospital.getLongitude() : 0.0;


    }


    // 병원 정보, 거리, 시간 용 생성자
    public  HospitalDto (Hospital hospital, double distanceKm, double durationSec) {
        this.id               = hospital.getId();
        this.name             = hospital.getName();
        this.address          = hospital.getAddress();
        this.phoneNumber      = hospital.getPhoneNumber();
        this.totalErCount     = hospital.getTotalErCount();
        this.availableErCount = hospital.getAvailableErCount();
        this.latitude = hospital.getLatitude() != null ? hospital.getLatitude() : 0.0;
        this.longitude = hospital.getLongitude() != null ? hospital.getLongitude() : 0.0;
        this.distanceKm = distanceKm;
        this.durationSec = durationSec;
    }
}

