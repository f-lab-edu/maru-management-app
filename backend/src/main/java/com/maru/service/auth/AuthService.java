package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.common.util.JwtUtil;
import com.maru.service.auth.dto.TokenPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    /**
     * 로그인
     */
    public TokenPair login() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Access Token 갱신
     */
    public TokenPair refreshAccessToken(String refreshToken) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Google OAuth Authorization URL 조회
     */
    public String getGoogleAuthorizationUrl() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Kakao OAuth Authorization URL 조회
     */
    public String getKakaoAuthorizationUrl() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Google OAuth 로그인 처리
     *
     * @param code Authorization Code
     * @return 인증 결과
     */
    public Object loginWithGoogle(String code) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Kakao OAuth 로그인 처리
     *
     * @param code Authorization Code
     * @return 인증 결과
     */
    public Object loginWithKakao(String code) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }
}
