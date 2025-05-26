package com.gandalp.gandalp.member.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NurseNameEmailDto {

    private Long id;
    private String name;
    private String email;
}
