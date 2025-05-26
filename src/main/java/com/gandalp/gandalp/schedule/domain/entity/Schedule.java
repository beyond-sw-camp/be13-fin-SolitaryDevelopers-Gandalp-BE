package com.gandalp.gandalp.schedule.domain.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleUpdateRequestDto;
import org.springframework.cglib.core.Local;

import com.gandalp.gandalp.common.entity.BaseEntity;
import com.gandalp.gandalp.hospital.domain.entity.Room;
import com.gandalp.gandalp.member.domain.entity.Nurse;

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

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Schedule extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule-id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nurse-id")
	private Nurse nurse;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Category category;

	@Column(nullable = false, length = 100)
	private String content;

	private LocalDateTime startTime;
	private LocalDateTime endTime;
	// 💡 Builder 수정: startTime, endTime을 시 단위로 절삭
	@Builder
	public Schedule(Nurse nurse, Category category, String content,
					LocalDateTime startTime, LocalDateTime endTime) {
		this.nurse = nurse;
		this.category = category;
		this.content = content;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public void updateSchedule(PersonalScheduleUpdateRequestDto personalScheduleUpdateRequestDto) {
		this.content = personalScheduleUpdateRequestDto.getContent();
		this.startTime = personalScheduleUpdateRequestDto.getStartTime();
		this.endTime = personalScheduleUpdateRequestDto.getEndTime();
	}

	public void setNurse(Nurse nurse) {
		this.nurse = nurse;
	}
}
