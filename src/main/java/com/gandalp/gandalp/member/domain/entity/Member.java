package com.gandalp.gandalp.member.domain.entity;


import com.gandalp.gandalp.common.entity.BaseEntity;
import com.gandalp.gandalp.hospital.domain.entity.Department;

import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.gandalp.gandalp.member.domain.dto.MemberUpdateDto;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
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
public class Member extends BaseEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member-id")
	private Long id;

	///  병원추가
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hospital-id")
	private Hospital hospital;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department-id")
	private Department department;

	@Column(nullable = false, unique = true, length = 50)
	private String accountId;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	private Type type;

	public void update(MemberUpdateDto updateDto){
		this.accountId = updateDto.getAccountId();
		if (updateDto.getPassword() != null) {
			this.password = updateDto.getPassword();
		}
	}
}
