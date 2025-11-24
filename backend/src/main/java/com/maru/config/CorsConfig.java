package com.maru.config;

import com.maru.config.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 설정
 *
 * httpOnly Cookie 기반 인증을 지원하기 위한 CORS 설정
 * - allowCredentials: true로 설정하여 Cookie 전송 허용
 * - allowedOrigins: 명시적 Origin 목록 (wildcard 사용 불가)
 * - Spring Security와 통합하여 인증 실패 시에도 CORS 헤더 적용
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    /**
     * CORS 설정을 CorsConfigurationSource Bean으로 등록
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(parseList(corsProperties.allowedOrigins()));
        config.setAllowedMethods(parseList(corsProperties.allowedMethods()));
        config.setAllowedHeaders(parseList(corsProperties.allowedHeaders()));

        config.setMaxAge(corsProperties.maxAge().toSeconds());
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 콤마(,) 로 구분된 corsProperties 문자열을 파싱해서 리스트로 반환
     *
     * @param value corsProperties 값
     * @return corsProperties 값이 파싱된 리스트(요소가 없으면 빈 리스트 반환)
     */
    private List<String> parseList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}