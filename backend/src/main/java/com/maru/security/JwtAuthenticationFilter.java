package com.maru.security;

import com.maru.common.exception.auth.AuthErrorCode;
import com.maru.common.exception.auth.AuthException;
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
            String token = extractToken(request);

            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            JwtUtil.TokenValidationResult validationResult = jwtUtil.validateAccessToken(token);
            if (validationResult == JwtUtil.TokenValidationResult.EXPIRED) {
                log.warn("만료된 JWT 토큰: {}", request.getRequestURI());
                exceptionResolver.resolveException(request, response, null, new AuthException(AuthErrorCode.TOKEN_EXPIRED));
                return;
            }
            if (validationResult != JwtUtil.TokenValidationResult.VALID) {
                log.warn("유효하지 않은 JWT 토큰: {}, 상태: {}", request.getRequestURI(), validationResult);
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtUtil.parseClaims(token);
            JwtClaims jwtClaims = JwtClaims.fromJwt(claims);

            try (AutoCloseable ignored = TenantContextHolder.withTenant(jwtClaims.tenantId());
                 MDC.MDCCloseable mdcUserId = MDC.putCloseable("userId", String.valueOf(jwtClaims.userId()))) {

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + jwtClaims.role())
                );

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        jwtClaims,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: userId={}, tenantId={}, dojangId={}, role={}, endpoint={}",
                        jwtClaims.userId(), jwtClaims.tenantId(), jwtClaims.dojangId(), jwtClaims.role(), request.getRequestURI());

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
