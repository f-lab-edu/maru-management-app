package com.maru.common.util;

import com.maru.service.auth.dto.TokenRes;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtil {
    private static final String COOKIE_TYPE_ACCESS = "accessToken";
    private static final String COOKIE_TYPE_REFRESH = "refreshToken";


    @Value("${jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Value("${cookie.secure:true}")
    private boolean secure;

    @Value("${cookie.http-only:true}")
    private boolean httpOnly;

    @Value("${cookie.path:/}")
    private String path;

    /**
     * 인증 토큰들을 Cookie로 설정하여 응답에 추가
     *
     * @param response HTTP 응답 객체
     * @param tokenRes JWT 토큰 정보 (Access Token, Refresh Token 포함)
     */
    public void setAuthCookies(HttpServletResponse response, TokenRes tokenRes) {
        Cookie accessTokenCookie = createCookie(
                COOKIE_TYPE_ACCESS,
            tokenRes.accessToken(),
            (int) accessTokenExpiration.toSeconds()
        );
        response.addCookie(accessTokenCookie);

        if (tokenRes.refreshToken() != null) {
            Cookie refreshTokenCookie = createCookie(
                    COOKIE_TYPE_REFRESH,
                tokenRes.refreshToken(),
                (int) refreshTokenExpiration.toSeconds()
            );
            response.addCookie(refreshTokenCookie);
        }
    }

    /**
     * Cookie 객체 생성
     *
     * @param name Cookie 이름
     * @param value Cookie 값
     * @param maxAge Cookie 만료 시간 (초 단위)
     * @return 생성된 Cookie 객체
     */
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
