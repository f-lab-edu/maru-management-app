package com.maru.security;

import com.maru.common.exception.auth.AuthErrorCode;
import com.maru.common.exception.auth.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * 데모 계정의 위험 API 접근을 차단하는 필터.
 * DELETE 요청은 데모 데이터 안정성을 위해 차단한다.
 */
@Component
public class DemoRestrictionFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver exceptionResolver;
    private final String demoUserId;

    public DemoRestrictionFilter(
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
        @Value("${maru.demo.user-id:}") String demoUserId
    ) {
        this.exceptionResolver = exceptionResolver;
        this.demoUserId = demoUserId;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (isDemoUser() && isBlockedRequest(request)) {
            exceptionResolver.resolveException(
                request, response, null, new AuthException(AuthErrorCode.DEMO_RESTRICTED)
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isDemoUser() {
        if (demoUserId == null || demoUserId.isBlank()) {
            return false;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        if (auth.getPrincipal() instanceof JwtClaims claims) {
            return demoUserId.equals(claims.userId());
        }

        return false;
    }

    private boolean isBlockedRequest(HttpServletRequest request) {
        return HttpMethod.DELETE.matches(request.getMethod());
    }
}
