package com.gandalp.gandalp.schedule.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.gandalp.gandalp.member.domain.entity.Type;
import com.gandalp.gandalp.schedule.domain.entity.Category;
import com.gandalp.gandalp.schedule.domain.entity.ScheduleTemp;
import com.gandalp.gandalp.schedule.domain.entity.TempCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.schedule.domain.entity.Schedule;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleRepositoryCustom {

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.nurse.id = :nurseId " +
            "AND s.endTime > :startTime " +
            "AND s.startTime < :endTime")
    List<Schedule> findOverlappingSchedules(@Param("nurseId") Long nurseId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);


    // 간호사 전체 일정 조회 (시작 ~ 끝)
    // 시작 시간이 범위에 들어오는 일정만 조회 ( day, evening, night 구분을 위해 만들었음)
    List<Schedule> findByNurseAndStartTimeBetween(Nurse nurse, LocalDateTime start, LocalDateTime end);


    List<Schedule> findAllByNurse(Nurse nurse);

    Optional<Schedule> findByNurseAndStartTimeAndCategory (Nurse nurse, LocalDateTime startTime, Category category);

    @Query("select s from Schedule s " +
            "where s.nurse IN :nurseList " +
            "and s.category = :category ")
    List<Schedule> findInNurseAndCategory(@Param("nurseList") List<Nurse> nurseList, @Param("category") Category category);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.nurse.id = :nurseId")
    List<Schedule> findByNurseId(@Param("nurseId") Long nurseId);

    // 일정 존재 여부
    boolean existsByNurseIdAndStartTimeLessThanEqualAndEndTimeGreaterThan(Long nurseId, LocalDateTime start, LocalDateTime end);

    // 특정 시간에 해당 nurse가 담당하는 schedule을 하나만 반환하는 메서드
    Optional<Schedule> findByNurseIdAndStartTimeLessThanEqualAndEndTimeGreaterThan(Long nurseId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.nurse.id = :nurseId " +
            "AND s.category =:category " +
            "AND FUNCTION('YEAR', s.startTime) = :year " +
            "AND FUNCTION('MONTH', s.startTime) = :month")
    List<Schedule> findByNurseAndMonth(@Param("nurseId") Long nurseId,
                                       @Param("year") int year,
                                       @Param("month") int month,
                                       @Param("category") Category category);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.nurse.id = :nurseId " +
            "AND s.category = :category" )
    List<Schedule> findByNurseAndCategory(@Param("nurseId") Long nurseId,
                                          @Param("category") Category category);


}
