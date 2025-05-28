package com.gandalp.gandalp.schedule.domain.entity;


import com.gandalp.gandalp.common.entity.BaseEntity;
import com.gandalp.gandalp.hospital.domain.entity.Room;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.schedule.domain.dto.WorkTempRequestUpdateDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleTemp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule-temp-id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse-id")
    private Nurse nurse;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TempCategory category;

    @Column(nullable = false, length = 100)
    private String content;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // ✨ 빌더 내부에서 시(hour) 단위로 절삭
    @Builder
    public ScheduleTemp(Nurse nurse, TempCategory category, String content,
                        LocalDateTime startTime, LocalDateTime endTime) {
        this.nurse = nurse;
        this.category = category;
        this.content = content;
        this.startTime = startTime != null ? startTime.truncatedTo(ChronoUnit.HOURS) : null;
        this.endTime = endTime != null ? endTime.truncatedTo(ChronoUnit.HOURS) : null;
    }

    public void acceptedOff(){
        this.category = TempCategory.ACCEPTED_OFF;
    }

    public void rejectedOff(){
        this.category = TempCategory.REJECTED_OFF;
    }

    public void update(Nurse nurse){
        this.nurse = nurse;
    }
}
