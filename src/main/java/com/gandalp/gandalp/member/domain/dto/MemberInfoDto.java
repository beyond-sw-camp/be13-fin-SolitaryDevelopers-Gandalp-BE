package com.gandalp.gandalp.member.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MemberInfoDto {

	private Long id;

	private String hospitalName;

	private String deptName;

	private String type;


}
