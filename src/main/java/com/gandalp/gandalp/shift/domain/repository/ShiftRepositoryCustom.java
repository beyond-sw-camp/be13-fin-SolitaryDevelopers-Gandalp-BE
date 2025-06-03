package com.gandalp.gandalp.shift.domain.repository;

import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.shift.domain.dto.ShiftResponseDto;
import com.gandalp.gandalp.shift.domain.entity.SearchOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShiftRepositoryCustom {


    Page<ShiftResponseDto> getSearchingAllByDepartment(Department department, String keyword, SearchOption searchOption, Pageable pageable);

    Page<ShiftResponseDto> getAllByDepartment(Department department, Pageable pageable);

}
