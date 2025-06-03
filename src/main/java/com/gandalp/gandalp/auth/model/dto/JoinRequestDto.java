package com.gandalp.gandalp.auth.model.dto;

import com.gandalp.gandalp.member.domain.entity.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinRequestDto {

    @NotBlank
    @Size(min = 2, max = 20)
    private String accountId;

    @NotBlank
    @Size(min = 4, max = 4)
    private String password;

    @NotNull(message = "회원 타입은 필수입니다.")
    private Type type;

    private String department;

    private String hospital;

}
