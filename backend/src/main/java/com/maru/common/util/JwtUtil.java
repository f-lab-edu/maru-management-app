package com.maru.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    /**
     * SecretKey 생성
     *
     * @return HMAC-SHA 알고리즘을 위한 SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param role 사용자 역할
     * @return 생성된 JWT 토큰
     */
    public String generateAccessToken(Long userId, Long tenantId, Long dojangId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("tenantId", tenantId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        return false;
    }

    /**
     * JWT 토큰을 파싱하여 Claims를 추출
     *
     * @param token 파싱할 JWT 토큰
     * @return JWT Claims 객체
     */
    public Claims parseClaims(String token) {
        return null;
    }

    /**
     * Refresh Token 생성
     *
     * @param userId 사용자 ID
     * @return 생성된 Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        return null;
    }

    /**
     * Refresh Token 유효성 검증
     *
     * @param token 검증할 Refresh Token
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateRefreshToken(String token) {
        return false;
    }
}
