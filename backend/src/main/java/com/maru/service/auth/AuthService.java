package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.common.exception.ErrorCode;
import com.maru.common.util.JwtUtil;
import com.maru.controller.auth.LoginReq;
import com.maru.service.auth.dto.TokenPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    /**
     * 테스트 로그인
     *
     * @param request 로그인 요청
     * @return 토큰 쌍 (Access Token, Refresh Token)
     */
    public TokenPair login(LoginReq request) {
        // TODO: 소셜 로그인 구현 시 실제 인증 로직으로 교체
        if (!"test@example.com".equals(request.getUsername()) ||
            !"test1234".equals(request.getPassword())) {
            throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다");
        }

        // 테스트용 사용자 정보
        Long userId = 1L;
        Long tenantId = 1L;
        Long dojangId = 1L;
        String role = "OWNER";

        String accessToken = jwtUtil.generateAccessToken(userId, tenantId, dojangId, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        log.debug("로그인 성공: userId={}, role={}", userId, role);

        return new TokenPair(accessToken, refreshToken, userId, role);
    }

    /**
     * Refresh Token을 사용하여 Access Token 갱신
     *
     * @param refreshToken Refresh Token
     * @return 새로운 토큰 쌍 (Access Token, Refresh Token)
     */
    public TokenPair refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(ErrorCode.AUTH_REFRESH_TOKEN_REQUIRED);
        }

        // Refresh Token 검증
        JwtUtil.TokenValidationResult validationResult =
            jwtUtil.validateRefreshToken(refreshToken);

        switch (validationResult) {
            case EXPIRED -> {
                log.warn("만료된 리프레시 토큰 사용 시도");
                throw new AuthException(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
            }
            case INVALID -> {
                log.warn("유효하지 않은 리프레시 토큰 사용 시도");
                throw new AuthException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
            }
        }

        // Refresh Token에서 사용자 정보 추출
        Long userId = jwtUtil.extractUserId(refreshToken);

        // TODO: 실제 구현 시 DB에서 사용자 정보 조회
        Long tenantId = 1L;
        Long dojangId = 1L;
        String role = "OWNER";

        // 새로운 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(userId, tenantId, dojangId, role);

        // Refresh Token 재사용
        String newRefreshToken = refreshToken;

        log.debug("토큰 갱신 성공: userId={}", userId);

        return new TokenPair(newAccessToken, newRefreshToken, userId, role);
    }
}
