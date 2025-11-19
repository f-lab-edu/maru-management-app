package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.common.util.JwtUtil;
import com.maru.service.auth.dto.GoogleTokenRes;
import com.maru.service.auth.dto.GoogleUserInfoRes;
import com.maru.service.auth.dto.TokenRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final GoogleOAuthService googleOAuthService;

    /**
     * 로그인
     */
    public TokenRes login() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Access Token 갱신
     */
    public TokenRes refreshAccessToken(String refreshToken) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Google OAuth Authorization URL 조회
     */
    public String getGoogleAuthorizationUrl() {
        return googleOAuthService.getAuthorizationUrl();
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
    public TokenRes loginWithGoogle(String code) {
        GoogleTokenRes tokenRes = googleOAuthService.exchangeCodeForToken(code);
        GoogleUserInfoRes userInfo = googleOAuthService.getUserInfo(tokenRes.accessToken());

        throw new UnsupportedOperationException("Repository 연동 필요");
    }

    /**
     * Kakao OAuth 로그인 처리
     *
     * @param code Authorization Code
     * @return 인증 결과
     */
    public TokenRes loginWithKakao(String code) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }
}
