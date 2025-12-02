package com.maru.security;

import com.maru.common.exception.AuthException;
import com.maru.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.jwtUtil = jwtUtil;
        this.exceptionResolver = exceptionResolver;
    }

    /**
     * HTTP 요청 시 JWT 토큰 검증 및 인증 정보 설정
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Cookie에서 JWT 토큰 추출
            String token = extractToken(request);

            // 토큰이 없으면 필터 체인 계속
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // JwtUtil을 사용한 토큰 검증
            JwtUtil.TokenValidationResult validationResult = jwtUtil.validateAccessToken(token);
            if (validationResult == JwtUtil.TokenValidationResult.EXPIRED) {
                log.warn("만료된 JWT 토큰: {}", request.getRequestURI());
                exceptionResolver.resolveException(request, response, null, new AuthException(AUTH_TOKEN_EXPIRED));
                return;
            }
            if (validationResult != JwtUtil.TokenValidationResult.VALID) {
                log.warn("유효하지 않은 JWT 토큰: {}, 상태: {}", request.getRequestURI(), validationResult);
                filterChain.doFilter(request, response);
                return;
            }

            // 유효한 토큰에서 Claims 추출
            Claims claims = jwtUtil.parseClaims(token);
            JwtClaims jwtClaims = JwtClaims.fromJwt(claims);

            // 테넌트 컨텍스트 + MDC userId 설정
            try (AutoCloseable ignored = TenantContextHolder.withTenant(jwtClaims.tenantId());
                 MDC.MDCCloseable mdcUserId = MDC.putCloseable("userId", String.valueOf(jwtClaims.userId()))) {

                // UsernamePasswordAuthenticationToken 생성 및 SecurityContext 설정
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + jwtClaims.role())
                );

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        jwtClaims,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 인증 성공 로깅
                log.debug("JWT 인증 성공: userId={}, tenantId={}, dojangId={}, role={}, endpoint={}",
                        jwtClaims.userId(), jwtClaims.tenantId(), jwtClaims.dojangId(), jwtClaims.role(), request.getRequestURI());

                // 필터 체인 계속
                filterChain.doFilter(request, response);
            }

        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Cookie에서 JWT 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 헬스체크 엔드포인트를 필터에서 제외
     *
     * @param request HttpServletRequest
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health");
    }
}
