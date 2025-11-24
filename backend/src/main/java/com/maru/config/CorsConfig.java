package com.maru.config;

import com.maru.config.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * CORS 설정
 *
 * httpOnly Cookie 기반 인증을 지원하기 위한 CORS 설정
 * - allowCredentials: true로 설정하여 Cookie 전송 허용
 * - allowedOrigins: 명시적 Origin 목록 (wildcard 사용 불가)
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration reg = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(corsProperties.maxAge().toSeconds());

        String[] origins = Arrays.stream(corsProperties.allowedOrigins().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (origins.length == 1 && "*".equals(origins[0])) {
            // Wildcard 사용 시 credentials 비활성화 (보안 제약)
            reg.allowedOriginPatterns("*").allowCredentials(false);
        } else {
            // 명시적 Origin 사용 시 credentials 활성화 (httpOnly Cookie 지원)
            reg.allowedOrigins(origins).allowCredentials(true);
        }
    }
}
