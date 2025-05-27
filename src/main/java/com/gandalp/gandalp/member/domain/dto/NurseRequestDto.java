package com.gandalp.gandalp.member.domain.dto;

import com.gandalp.gandalp.member.domain.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NurseRequestDto {

    private String name;

    private String email;

    private Type type;

    private String password;

}
