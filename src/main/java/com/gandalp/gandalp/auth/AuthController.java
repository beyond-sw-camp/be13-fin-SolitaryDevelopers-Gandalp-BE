package com.gandalp.gandalp.auth;

import com.gandalp.gandalp.auth.jwt.JwtTokenProvider;
import com.gandalp.gandalp.auth.model.dto.JoinRequestDto;
import com.gandalp.gandalp.auth.model.dto.LoginRequestDto;
import com.gandalp.gandalp.auth.model.dto.TokenResponseDto;
import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.Type;
import com.gandalp.gandalp.member.domain.repository.MemberRepository;
import com.gandalp.gandalp.member.domain.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "01 인증 API", description = "인증 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/join")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> join(@Valid @RequestBody JoinRequestDto dto) {

        Map<String, String> response = new HashMap<>();
        try{
            Hospital hospital = authService.getLoginMember().getHospital();
            authService.join(dto, hospital);
            response.put("message", "회원가입 완료");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok("회원가입");
    }

    /**
     * 로그인 API
     * - accountId, password 입력받아 JWT 토큰 발급
     */
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto, HttpServletResponse response) {
        TokenResponseDto tokenResponse = null;
        try {
            tokenResponse = authService.login(dto, response);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).body(Map.of("error","존재하지 않는 계정입니다. <br> 아이디와 비밀번호를 정확히 입력해 주세요."));
        } catch (BadCredentialsException e){
            return ResponseEntity.status(401).body(Map.of("error","비밀번호가 틀렸습니다. <br> 아이디와 비밀번호를 정확히 입력해 주세요."));
        }catch (Exception e){
            return ResponseEntity.status(500).body(Map.of("error","다시 입력해주세요."));
        }

        return ResponseEntity.ok(createSuccessResponse("로그인 성공", tokenResponse));
    }

    /**
     * 로그아웃 API
     * - Access Token 을 블랙리스트 등록
     */
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken, HttpServletResponse response) {

        try{
            authService.logout(bearerToken);

            // refresh 쿠키 삭제 !!
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

            response.addHeader("Set-Cookie", deleteCookie.toString());

        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok("로그아웃 성공");
    }

    /**
     * Refresh 토큰으로 Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        TokenResponseDto tokenResponse = null;
        try{
            tokenResponse = authService.refresh(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok(tokenResponse);
    }

    // ✅ 내부 응답 포맷 정리
    private Map<String, Object> createSuccessResponse(String message, TokenResponseDto tokenResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("accessToken", tokenResponse.getAccessToken());
        // response.put("refreshToken", tokenResponse.getRefreshToken());
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String accountId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("accountId", accountId);
        return response;
    }
}
