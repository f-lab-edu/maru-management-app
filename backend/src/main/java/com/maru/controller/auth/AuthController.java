package com.maru.controller.auth;

import com.maru.service.auth.AuthService;
import com.maru.service.auth.dto.TokenPair;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;


// TODO: 소셜 로그인 구현 시 교체 예정
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
        TokenPair tokenPair = authService.login(request);

        ResponseCookie accessTokenCookie = createCookie(
            "accessToken",
            tokenPair.getAccessToken(),
            accessTokenExpiration
        );

        ResponseCookie refreshTokenCookie = createCookie(
            "refreshToken",
            tokenPair.getRefreshToken(),
            refreshTokenExpiration
        );

        LoginRes response = new LoginRes(
            tokenPair.getUserId(),
            tokenPair.getRole(),
            "로그인 성공"
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    /**
     * Access Token 갱신 API
     *
     * @param refreshToken RefreshToken
     * @return 갱신된 토큰 응답
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginRes> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        TokenPair tokenPair = authService.refreshAccessToken(refreshToken);

        ResponseCookie accessTokenCookie = createCookie(
            "accessToken",
            tokenPair.getAccessToken(),
            accessTokenExpiration
        );

        ResponseCookie refreshTokenCookie = createCookie(
            "refreshToken",
            tokenPair.getRefreshToken(),
            refreshTokenExpiration
        );

        LoginRes response = new LoginRes(
            tokenPair.getUserId(),
            tokenPair.getRole(),
            "토큰 갱신 성공"
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    /**
     * httpOnly Cookie 생성 헬퍼 메서드
     */
    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
    }
}
