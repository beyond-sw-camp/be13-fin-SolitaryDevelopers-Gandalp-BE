package com.gandalp.gandalp.schedule.domain.repository;

import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.schedule.domain.entity.Category;
import com.gandalp.gandalp.schedule.domain.entity.Schedule;
import com.gandalp.gandalp.schedule.domain.entity.ScheduleTemp;
import com.gandalp.gandalp.schedule.domain.entity.TempCategory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleTempRepository extends JpaRepository<ScheduleTemp, Long> {

    Page<ScheduleTemp> findAllByNurseEmailContaining(String email, Pageable pageable);

    Optional<ScheduleTemp> findById(Long schduleTempId);

    @Query("SELECT s FROM ScheduleTemp s " +
            "WHERE s.nurse.id = :nurseId " +
            "AND s.endTime > :startTime " +
            "AND s.startTime < :endTime")
    List<ScheduleTemp> findOverlappingTempSchedules(@Param("nurseId") Long nurseId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    Page<ScheduleTemp> findAllByNurse(Nurse nurse, Pageable pageable);

    @Query("select s from ScheduleTemp s " +
            "where s.nurse IN :nurseList " +
            "AND s.category <> :category")
    Page<ScheduleTemp> findAllOffByCategory(@Param("nurseList") List<Nurse> nurseList, @Param("category") TempCategory category, Pageable pageable);

    @Query("select s from ScheduleTemp s " +
            "where s.nurse IN :nurseList " +
            "AND s.category = :category")
    List<ScheduleTemp> findAllWorkByCategory(@Param("nurseList") List<Nurse> nurseList, @Param("category") TempCategory category);

    void deleteAllByCategory(TempCategory category);

    @Query("SELECT s FROM ScheduleTemp s " +
            "WHERE s.nurse.id = :nurseId " +
            "AND s.category =:category ")
    List<ScheduleTemp> findByNurseAndCategory(@Param("nurseId") Long nurseId,
                                       @Param("category") TempCategory category);


}
