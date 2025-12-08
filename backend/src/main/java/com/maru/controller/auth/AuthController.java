package com.maru.controller.auth;

import com.maru.common.exception.AuthException;
import com.maru.common.util.CookieUtil;

import static com.maru.common.exception.ErrorCode.*;
import com.maru.controller.auth.dto.OAuthCallbackReq;
import com.maru.controller.auth.dto.OAuthUrlRes;
import com.maru.domain.user.OAuthProvider;
import com.maru.service.auth.AuthService;
import com.maru.controller.auth.dto.TokenRes;
import com.maru.security.CurrentUserId;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * Access Token 갱신 API
     *
     * @param refreshToken Refresh Token (쿠키)
     * @param response HTTP 응답
     * @return 응답
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
        @CookieValue(value = "refreshToken", required = false) String refreshToken,
        HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AUTH_REFRESH_TOKEN_REQUIRED);
        }

        TokenRes tokenRes = authService.refreshAccessToken(refreshToken);
        cookieUtil.setAuthCookies(response, tokenRes);

        return ResponseEntity.ok().build();
    }

    /**
     * 도장 선택 후 토큰 재발급
     *
     * @param userId 현재 인증된 사용자 ID
     * @param dojangId 선택할 도장 ID
     * @param response HTTP 응답
     * @return 재발급된 토큰 정보
     */
    @PostMapping("/select-dojang")
    public ResponseEntity<Void> selectDojang(
        @CurrentUserId Long userId,
        @RequestParam @NotNull Long dojangId,
        HttpServletResponse response
    ) {
        if (userId == null) {
            throw new AuthException(AUTH_REQUIRED);
        }

        TokenRes tokenRes = authService.selectDojang(userId, dojangId);
        cookieUtil.setAuthCookies(response, tokenRes);

        return ResponseEntity.noContent().build();
    }

    /**
     * Google OAuth Authorization URL 조회
     *
     * @return Google OAuth 인증 URL
     */
    @GetMapping("/oauth/google")
    public ResponseEntity<OAuthUrlRes> getGoogleAuthorizationUrl() {
        String authUrl = authService.getAuthorizationUrl(OAuthProvider.GOOGLE);
        return ResponseEntity.ok(new OAuthUrlRes(authUrl));
    }

    /**
     * Google OAuth Callback 처리
     *
     * @param request Authorization Code가 포함된 요청
     * @param response HTTP 응답
     * @return 응답
     */
    @PostMapping("/oauth/callback/google")
    public ResponseEntity<Void> handleGoogleCallback(
        @Valid @RequestBody OAuthCallbackReq request,
        HttpServletResponse response) {

        TokenRes tokenRes = authService.loginWithOAuth(OAuthProvider.GOOGLE, request.code());
        cookieUtil.setAuthCookies(response, tokenRes);

        return ResponseEntity.ok().build();
    }

    /**
     * Kakao OAuth Authorization URL 조회
     *
     * @return Kakao OAuth 인증 URL
     */
    @GetMapping("/oauth/kakao")
    public ResponseEntity<OAuthUrlRes> getKakaoAuthorizationUrl() {
        String authUrl = authService.getAuthorizationUrl(OAuthProvider.KAKAO);
        return ResponseEntity.ok(new OAuthUrlRes(authUrl));
    }

    /**
     * Kakao OAuth Callback 처리
     *
     * @param request Authorization Code가 포함된 요청
     * @param response HTTP 응답
     * @return 응답
     */
    @PostMapping("/oauth/callback/kakao")
    public ResponseEntity<Void> handleKakaoCallback(
        @Valid @RequestBody OAuthCallbackReq request,
        HttpServletResponse response) {

        TokenRes tokenRes = authService.loginWithOAuth(OAuthProvider.KAKAO, request.code());
        cookieUtil.setAuthCookies(response, tokenRes);

        return ResponseEntity.ok().build();
    }
}
