package com.gandalp.gandalp.member.domain.entity;

import java.util.UUID;

import com.gandalp.gandalp.common.entity.BaseEntity;
import com.gandalp.gandalp.hospital.domain.entity.Department;

import com.gandalp.gandalp.member.domain.dto.NurseUpdateDto;
import com.gandalp.gandalp.schedule.domain.entity.SurgerySchedule;
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
import jakarta.persistence.PrePersist;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Nurse extends BaseEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "nurse-id")
	private Long id;

	@Column(unique = true, length = 36)
	private String no;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department-id")
	private Department department;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Type type;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, length = 50)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	private Status workingStatus;

	@PrePersist // 객체 생성되면 자동 주입
	public void assignUuid(){
		if(this.no == null){
			this.no = UUID.randomUUID().toString();
		}
	}

	public void update(NurseUpdateDto updateDto){
		this.name = updateDto.getName();
		this.email = updateDto.getEmail();
	}

	public void updateWorkingStatus(Status workingStatus) {
		this.workingStatus = workingStatus;
	}


}
