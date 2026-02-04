package com.maru.config;

import com.maru.security.DemoRestrictionFilter;
import com.maru.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DemoRestrictionFilter demoRestrictionFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // CSRF 보호 비활성화 (JWT + Stateless 방식이므로 불필요)
            .csrf(AbstractHttpConfigurer::disable)

            // 세션을 생성하거나 사용하지 않음
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // HTTP Basic 인증 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)

            // Form 로그인 비활성화
            .formLogin(AbstractHttpConfigurer::disable)

            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 URL (로그인, 회원가입, 헬스체크, SMS 인증)
                .requestMatchers(
                        "/api/v1/auth/**",
                        "/api/v1/sms/**",
                        "/api/v1/pay/**",
                        "/api/v1/dev/**",  // TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.
                        "/api/v1/dojangs/**", // TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.
                        "/internal/healthz",
                        "/actuator/health",
                        "/favicon.ico",
                        "/error",
                        // Swagger UI
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/docs.html",
                        "/v3/api-docs/**",
                        "/webjars/**"
                ).permitAll()

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // JWT 인증 필터를 Spring Security 필터 체인에 추가
            // UsernamePasswordAuthenticationFilter 앞에 실행됨
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 데모 사용자 제한 필터 (JWT 인증 이후 실행)
            .addFilterAfter(demoRestrictionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
