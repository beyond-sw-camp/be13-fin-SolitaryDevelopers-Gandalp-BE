package com.gandalp.gandalp.hospital.domain.repository;

import com.gandalp.gandalp.hospital.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r " +
            "WHERE r.hospital.id = :hospitalId")
    List<Room> findByHospitalId(@Param("hospitalId") Long hospitalId);
}
