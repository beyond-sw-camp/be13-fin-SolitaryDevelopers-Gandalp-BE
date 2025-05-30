package com.gandalp.gandalp.member.domain.dto;

import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.entity.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NurseCurrentStatusDto {

	private Long id;
	private String name;
	private String codeLabel;


	public NurseCurrentStatusDto(Nurse nurse){
		this.id = nurse.getId();
		this.name = nurse.getName();
		this.codeLabel = String.valueOf(nurse.getWorkingStatus());

	}
}
