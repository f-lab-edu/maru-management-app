package com.maru.security;

import io.jsonwebtoken.Claims;

import java.util.Map;

public record JwtClaims(
        Long userId,
        Long tenantId,
        Long dojangId,
        String role
) {
    public static JwtClaims from(Map<String, Object> claims) {
        return new JwtClaims(
                asLong(claims.get("userId")),
                asLong(claims.get("tenantId")),
                asLong(claims.get("dojangId")),
                asString(claims.get("role"))
        );
    }

    public static JwtClaims fromJwt(Claims claims) {
        return new JwtClaims(
                Long.parseLong(claims.getSubject()),
                asLong(claims.get("tenantId")),
                asLong(claims.get("dojangId")),
                claims.get("role", String.class)
        );
    }

    private static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private static String asString(Object value) {
        if (value instanceof String string) {
            return string;
        }
        return null;
    }
}
