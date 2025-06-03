package com.gandalp.gandalp.auth.model.service;

import com.gandalp.gandalp.auth.model.dto.JoinRequestDto;
import com.gandalp.gandalp.auth.model.dto.LoginRequestDto;
import com.gandalp.gandalp.auth.model.dto.TokenResponseDto;
import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.gandalp.gandalp.member.domain.entity.Member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {


    void join(JoinRequestDto dto, Hospital hospital);

    TokenResponseDto login(LoginRequestDto dto, HttpServletResponse response);

    void logout(String bearerToken);
    
    Member loadMemberByToken(String token); // 토큰으로 이름만 가져옴

    TokenResponseDto refresh(HttpServletRequest request);

    Member getLoginMember();

    void validateDuplicateEmail(String email);

}
