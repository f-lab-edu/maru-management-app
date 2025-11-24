package com.maru.common.util;

import com.maru.config.properties.CookieProperties;
import com.maru.config.properties.JwtProperties;
import com.maru.service.auth.dto.TokenRes;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieUtil {
    private static final String COOKIE_TYPE_ACCESS = "accessToken";
    private static final String COOKIE_TYPE_REFRESH = "refreshToken";

    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    /**
     * 인증 토큰들을 Cookie로 설정하여 응답에 추가
     *
     * @param response HTTP 응답 객체
     * @param tokenRes JWT 토큰 정보 (Access Token, Refresh Token 포함)
     */
    public void setAuthCookies(HttpServletResponse response, TokenRes tokenRes) {
        ResponseCookie accessTokenCookie = createCookie(
                COOKIE_TYPE_ACCESS,
            tokenRes.accessToken()
        );
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        if (tokenRes.refreshToken() != null) {
            ResponseCookie refreshTokenCookie = createCookie(
                    COOKIE_TYPE_REFRESH,
                tokenRes.refreshToken()
            );
            response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        }
    }

    /**
     * ResponseCookie 객체 생성
     *
     * @param name Cookie 이름
     * @param value Cookie 값
     * @return 생성된 ResponseCookie 객체
     */
    private ResponseCookie createCookie(String name, String value) {
        return ResponseCookie.from(name, value)
            .httpOnly(cookieProperties.httpOnly())
            .secure(cookieProperties.secure())
            .path(cookieProperties.path())
            .maxAge(cookieProperties.maxAge())
            .sameSite(cookieProperties.sameSite())
            .build();
    }
}
