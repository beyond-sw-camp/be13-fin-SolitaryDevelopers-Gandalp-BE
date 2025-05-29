package com.gandalp.gandalp.hospital.domain.repository;

import com.gandalp.gandalp.hospital.domain.dto.HospitalDto;
import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.gandalp.gandalp.hospital.domain.entity.SortOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HospitalRepositoryCustom {

    // 가까운 병원 20곳 조회
    List<HospitalDto> searchNearbyHospitals(List<Long> hospitalIds, String keyword, SortOption sortOption);
}
