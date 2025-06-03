package com.gandalp.gandalp.auth.model.service;

import java.util.Arrays;
import java.util.Optional;

import com.gandalp.gandalp.auth.jwt.JwtTokenProvider;
import com.gandalp.gandalp.auth.model.dto.CustomUserDetails;
import com.gandalp.gandalp.auth.model.dto.JoinRequestDto;
import com.gandalp.gandalp.auth.model.dto.LoginRequestDto;
import com.gandalp.gandalp.auth.model.dto.TokenResponseDto;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.gandalp.gandalp.hospital.domain.repository.DepartmentRepository;
import com.gandalp.gandalp.hospital.domain.repository.HospitalRepository;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Type;
import com.gandalp.gandalp.member.domain.repository.MemberRepository;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final NurseRepository nurseRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;

    @Override
    @Transactional   // 해당 병원 관리자가 할 수 있는 거 ~~
    public void join(JoinRequestDto dto, Hospital hospital) {
        String accountId = dto.getAccountId();
        String password = dto.getPassword();


        // 아이디 중복 체크
        if (memberRepository.findByAccountId(accountId).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 계정입니다.");
        }


        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        Department department = departmentRepository.findByNameAndHospital(dto.getDepartment(), hospital)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 부서가 없습니다."));


        Member member = Member.builder()
                .accountId(accountId)
                .password(encodedPassword)
                .type(dto.getType()) // 기본값: PARAMEDIC
                .hospital(hospital)
                .department(department)
                .build();

        memberRepository.save(member);


        log.info("회원가입 완료 - accountId: {}", accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto dto, HttpServletResponse response) {

        String accountId = dto.getAccountId();
        String password = dto.getPassword();

        Member member = memberRepository.findByAccountId(accountId).orElseThrow(
            () -> new UsernameNotFoundException("해당 계정이 존재하지 않습니다.")
        );


        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getAccountId(), member.getType().name());

        String refreshToken = jwtTokenProvider.createRefreshToken(member.getAccountId());

        // 보통 accessToken 은 body 로 전달해주고 refreshtoken은 httpOnly 쿠키로 전달해준다
       ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
           .httpOnly(true)
           .secure(false) // HTTPS 환경으로 할거면 True로 변경
           .path("/")
           .maxAge(7 * 24 * 60 * 60)
           .sameSite("Lax")
           .build();

       response.addHeader("Set-Cookie", refreshCookie.toString());


        return new TokenResponseDto(accessToken);
    }

    @Override
    @Transactional
    public void logout(String bearerToken) {
        String accessToken = jwtTokenProvider.resolveToken(bearerToken)
                .orElseThrow(() -> new IllegalArgumentException("토큰이 존재하지 않습니다."));

        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // 1. accessToken 블랙리스트 등록
        jwtTokenProvider.addBlacklist(accessToken);

        // 2. accountId 추출해서 refreshToken 삭제해주기
        jwtTokenProvider.deleteRefreshToken(accessToken);

    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponseDto refresh(HttpServletRequest request) {

        // 1. 쿠키에서 refreshToken 꺼내기
        String refreshToken = extractTokenFromCookie(request, "refreshToken").orElseThrow(
            () -> new IllegalArgumentException("Refresh Token 쿠키가 존재하지 않습니다.")
        );


        //  2. 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // 3. 사용자 정보 추출 및 accessToken 재발급
        Member member = memberRepository.findByAccountId(jwtTokenProvider.getUserName(refreshToken))
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        String accessToken = jwtTokenProvider.createAccessToken(member.getAccountId(), member.getType().name());

        return new TokenResponseDto(accessToken);
    }

    // 토큰 추출 유틸 메서드
    private Optional<String> extractTokenFromCookie(HttpServletRequest request, String cookieName){
        if (request.getCookies() == null)
            return Optional.empty();

        return Arrays.stream(request.getCookies())
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    @Override
    public Member getLoginMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("유효하지 않은 사용자입니다.");
        }

        return memberRepository.findById(userDetails.getMember().getId())
            .orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));
    }

    @Override
    public void validateDuplicateEmail(String email) {
        if (nurseRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + email);
        }
    }


    @Override
    public Member loadMemberByToken(String token) {
        if(!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }

        String accountId = jwtTokenProvider.getUserName(token);
        return memberRepository.findByAccountId(accountId).orElseThrow(
                () -> new UsernameNotFoundException("회원 정보를 찾을 수 없습니다."+ accountId)
        );
    }



}
