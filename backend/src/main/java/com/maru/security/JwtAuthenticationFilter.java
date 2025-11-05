package com.maru.security;

import com.maru.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Authorization 헤더에서 Bearer 토큰 추출
            String token = extractToken(request);

            // 토큰이 없으면 필터 체인 계속
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // JwtUtil을 사용한 토큰 검증
            if (!jwtUtil.validateAccessToken(token)) {
                log.warn("유효하지 않은 JWT 토큰: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 유효한 토큰에서 Claims 추출
            Claims claims = jwtUtil.parseClaims(token);
            Long userId = Long.parseLong(claims.getSubject());
            Long tenantId = claims.get("tenantId", Long.class);
            Long dojangId = claims.get("dojangId", Long.class);
            String role = claims.get("role", String.class);

            // 테넌트 컨텍스트 설정
            TenantContextHolder.setTenantId(tenantId);

            // UsernamePasswordAuthenticationToken 생성 및 SecurityContext 설정
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
            );

            // TODO : UsernamePasswordAuthenticationToken -> Oauth2 의존성을 추가해서 JwtAuthenticationToken 등으로 리팩토링 고민
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 인증 성공 로깅
            log.info("JWT 인증 성공: userId={}, tenantId={}, dojangId={}, role={}, ip={}, endpoint={}",
                     userId, tenantId, dojangId, role,
                     request.getRemoteAddr(),
                     request.getRequestURI());

        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
        } finally {
            // ThreadLocal 정리
            // TODO : try with resource 로 리팩토링 고민
            TenantContextHolder.clear();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
