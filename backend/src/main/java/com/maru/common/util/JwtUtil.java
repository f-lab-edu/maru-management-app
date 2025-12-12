package com.maru.common.util;

import com.maru.config.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public enum TokenValidationResult {
        VALID,
        EXPIRED,
        INVALID
    }

    /**
     * SecretKey 조회
     *
     * @return HMAC-SHA 알고리즘을 위한 SecretKey
     */
    private SecretKey getSigningKey() {
        return this.key;
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
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(userId.toString())
                .issuer("maru-management-api")
                .audience().add("maru-management-client").and()
                .claim("type", TOKEN_TYPE_ACCESS)
                .claim("tenantId", tenantId)
                .claim("dojangId", dojangId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Access Token 유효성 검증 (상태 구분)
     *
     * @param token 검증할 Access Token
     * @return 토큰 검증 결과 (VALID/EXPIRED/INVALID)
     */
    public TokenValidationResult validateAccessToken(String token) {
        return validateToken(token, TOKEN_TYPE_ACCESS);
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
        return generateRefreshToken(userId, null, null, null);
    }

    /**
     * Refresh Token 생성 (테넌트/도장/역할 포함)
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param role 사용자 역할
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId, Long tenantId, Long dojangId, String role) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtProperties.refreshTokenExpiration());

        return Jwts.builder()
                .subject(userId.toString())
                .issuer("maru-management-api")
                .audience().add("maru-management-client").and()
                .claim("type", TOKEN_TYPE_REFRESH)
                .claim("tenantId", tenantId)
                .claim("dojangId", dojangId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Refresh Token 유효성 검증 (상태 구분)
     *
     * @param token 검증할 Refresh Token
     * @return 토큰 검증 결과 (VALID/EXPIRED/INVALID)
     */
    public TokenValidationResult validateRefreshToken(String token) {
        return validateToken(token, TOKEN_TYPE_REFRESH);
    }

    /**
     * JWT 토큰 유효성 검증 (공통 로직)
     *
     * @param token 검증할 토큰
     * @param expectedType 기대하는 토큰 타입
     * @return 토큰 검증 결과 (VALID/EXPIRED/INVALID)
     */
    private TokenValidationResult validateToken(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            if (!expectedType.equals(tokenType)) {
                return TokenValidationResult.INVALID;
            }

            return TokenValidationResult.VALID;
        } catch (ExpiredJwtException e) {
            return TokenValidationResult.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            return TokenValidationResult.INVALID;
        }
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }
}
