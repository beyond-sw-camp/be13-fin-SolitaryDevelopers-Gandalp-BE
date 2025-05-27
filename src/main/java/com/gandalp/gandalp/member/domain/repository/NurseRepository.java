package com.gandalp.gandalp.member.domain.repository;

import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.member.domain.dto.NurseCurrentStatusDto;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NurseRepository extends JpaRepository<Nurse, Long>, NurseRepositoryCustom {

    Optional<Nurse> findByEmail(String email);

    List<Nurse> findByDepartment(Department department);

    Optional<Nurse> findByPasswordAndEmail(String password, String email);

    Optional<Nurse> findByName(String name);

    Optional<Nurse> findByNo(String no);
}
