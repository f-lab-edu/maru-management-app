package com.maru.controller.auth;

import com.maru.common.util.CookieUtil;
import com.maru.controller.auth.dto.OAuthCallbackReq;
import com.maru.controller.auth.dto.OAuthUrlRes;
import com.maru.service.auth.AuthService;
import com.maru.service.auth.dto.TokenRes;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * 로그인 API
     *
     * @return 응답
     */
    @PostMapping("/login")
    public ResponseEntity<?> login() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Access Token 갱신 API
     *
     * @param refreshToken Refresh Token (쿠키)
     * @param response HTTP 응답
     * @return 응답
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
        @CookieValue("refreshToken") String refreshToken,
        HttpServletResponse response) {

        TokenRes tokenRes = authService.refreshAccessToken(refreshToken);
        cookieUtil.setAuthCookies(response, tokenRes);

        return ResponseEntity.ok().build();
    }

    /**
     * Google OAuth Authorization URL 조회
     *
     * @return Google OAuth 인증 URL
     */
    @GetMapping("/oauth/google")
    public ResponseEntity<OAuthUrlRes> getGoogleAuthorizationUrl() {
        String authUrl = authService.getGoogleAuthorizationUrl();
        OAuthUrlRes response = new OAuthUrlRes(authUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Google OAuth Callback 처리
     *
     * @param request Authorization Code가 포함된 요청
     * @param response HTTP 응답
     * @return 응답
     */
    @PostMapping("/oauth/google/callback")
    public ResponseEntity<Void> handleGoogleCallback(
        @Valid @RequestBody OAuthCallbackReq request,
        HttpServletResponse response) {

        TokenRes tokenRes = authService.loginWithGoogle(request.code());
        cookieUtil.setAuthCookies(response, tokenRes);

        return ResponseEntity.ok().build();
    }

    /**
     * Kakao OAuth Authorization URL 조회
     *
     * @return Kakao OAuth 인증 URL
     */
    @GetMapping("/oauth/kakao")
    public ResponseEntity<?> getKakaoAuthorizationUrl() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Kakao OAuth Callback 처리
     *
     * @param request Authorization Code가 포함된 요청
     * @return 응답
     */
    @PostMapping("/oauth/kakao/callback")
    public ResponseEntity<?> handleKakaoCallback(@RequestBody Map<String, String> request) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }
}
