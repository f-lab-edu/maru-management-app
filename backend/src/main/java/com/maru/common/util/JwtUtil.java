package com.maru.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

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
     * @return JWT 토큰
     */
    public String generateAccessToken(Long userId, Long tenantId, Long dojangId, String role) {
        Date now = new Date();
        Date expiryDate = Date.from(Instant.now().plus(accessTokenExpiration));

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer("maru-management-api")
                .setAudience("maru-management-client")
                .claim("type", TOKEN_TYPE_ACCESS)
                .claim("tenantId", tenantId)
                .claim("dojangId", dojangId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Access Token 유효성 검증
     *
     * @param token 검증할 Access Token
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 토큰 타입 검증
            String tokenType = claims.get("type", String.class);
            if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (UnsupportedJwtException e) {
            return false;
        } catch (MalformedJwtException e) {
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWT 토큰을 파싱하여 Claims를 추출
     *
     * @param token 파싱할 JWT 토큰
     * @return JWT Claims 객체
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Refresh Token 생성
     *
     * @param userId 사용자 ID
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = Date.from(Instant.now().plus(refreshTokenExpiration));

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer("maru-management-api")
                .setAudience("maru-management-client")
                .claim("type", TOKEN_TYPE_REFRESH)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Refresh Token 유효성 검증
     *
     * @param token 검증할 Refresh Token
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 토큰 타입 검증
            String tokenType = claims.get("type", String.class);
            if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (UnsupportedJwtException e) {
            return false;
        } catch (MalformedJwtException e) {
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
