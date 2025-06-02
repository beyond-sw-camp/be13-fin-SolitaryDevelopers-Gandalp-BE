package com.gandalp.gandalp.schedule.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gandalp.gandalp.schedule.domain.entity.SurgerySchedule;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SurgeryScheduleRepository extends JpaRepository<SurgerySchedule,Long>, SurgeryScheduleRepositoryCustom {

    @Query("SELECT s FROM SurgerySchedule s " +
            "WHERE s.room.id = :roomId " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime")
    List<SurgerySchedule> findOverlappingSchedules(@Param("roomId") Long roomId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM SurgerySchedule s WHERE s.room.id IN :roomIds")
    List<SurgerySchedule> findByRoomIdIn(@Param("roomIds") List<Long> roomIds);

}
