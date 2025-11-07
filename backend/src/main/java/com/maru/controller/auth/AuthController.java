package com.maru.controller.auth;

import com.maru.common.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 인증 컨트롤러
 *
 * TODO: 소셜 로그인 구현 시 교체 예정
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @Value("${jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    /**
     * 로그인 테스트 API
     *
     * TODO: 소셜 로그인 구현 시 교체 예정
     *
     * @param request 로그인 요청
     * @return 로그인 응답 (토큰은 Cookie로 전달)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(@Valid @RequestBody LoginReq request) {
        // TODO: 소셜 로그인 구현 시 실제 인증 로직으로 교체
        if (!"test@example.com".equals(request.getUsername()) || !"test1234".equals(request.getPassword())) {
            throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다");
        }

        // 테스트용 사용자 정보
        Long userId = 1L;
        Long tenantId = 1L;
        Long dojangId = 1L;
        String role = "OWNER";

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(userId, tenantId, dojangId, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        // httpOnly Cookie 설정
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(accessTokenExpiration)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("Strict")
                .build();

        log.debug("로그인 성공: userId={}, role={}", userId, role);

        LoginRes response = new LoginRes(userId, role, "로그인 성공");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }
}
