package com.gandalp.gandalp.shift.domain.repository;

import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.shift.domain.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Board, Long>, ShiftRepositoryCustom {

    @Query("SELECT b FROM Board b LEFT JOIN FETCH b.comments WHERE b.id = :boardId")
    Optional<Board> findByIdWithComments(@Param("boardId") Long boardId);

    @Query(
        value = "SELECT b FROM Board b WHERE b.department = :department",
        countQuery = "SELECT COUNT(b) FROM Board b WHERE b.department = :department"
    )

    Page<Board> findAllByDepartment(@Param("department") Department department, Pageable pageable);


//    Page<Board> findAllByDepartment(Department department, Pageable pageable);


}
