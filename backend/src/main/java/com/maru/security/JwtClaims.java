package com.maru.security;

import io.jsonwebtoken.Claims;

import java.util.Map;

public record JwtClaims(
        String userId,
        String tenantId,
        String dojangId,
        String role
) {
    public static JwtClaims from(Map<String, Object> claims) {
        return new JwtClaims(
                asString(claims.get("userId")),
                asString(claims.get("tenantId")),
                asString(claims.get("dojangId")),
                asString(claims.get("role"))
        );
    }

    public static JwtClaims fromJwt(Claims claims) {
        return new JwtClaims(
                claims.getSubject(),
                asString(claims.get("tenantId")),
                asString(claims.get("dojangId")),
                claims.get("role", String.class)
        );
    }

    private static String asString(Object value) {
        if (value instanceof String string) {
            return string;
        }
        return null;
    }
}
