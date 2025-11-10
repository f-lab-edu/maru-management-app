package com.maru.common.exception;

import com.maru.controller.common.ErrorRes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.maru.common.exception.ErrorCode.AUTH_ACCESS_DENIED;
import static com.maru.common.exception.ErrorCode.AUTH_REQUIRED;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * AuthException 처리 (401 Unauthorized)
     * ErrorCode를 포함한 인증 예외 - 메시지 중복 없음
     *
     * @param ex AuthException 인스턴스
     * @param request HTTP 요청 정보
     * @return 401 상태 코드와 ErrorRes 응답
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorRes> handleAuthException(
            AuthException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();

        log.warn("인증 실패: code={}, path={}, error={}",
                errorCode.getCode(), request.getRequestURI(), ex.getMessage());

        ErrorRes errorResponse = ErrorRes.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                errorCode.getCode(),
                errorCode.getMessage(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * AuthenticationException 처리 (401 Unauthorized)
     * Spring Security 기본 예외 처리 (fallback)
     *
     * @param ex AuthenticationException 인스턴스
     * @param request HTTP 요청 정보
     * @return 401 상태 코드와 ErrorRes 응답
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRes> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("인증 실패: path={}, error={}",
                request.getRequestURI(), ex.getMessage());

        ErrorRes errorResponse = ErrorRes.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                AUTH_REQUIRED.getCode(),
                AUTH_REQUIRED.getMessage(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * AccessDeniedException 처리 (403 Forbidden)
     *
     * @param ex AccessDeniedException 인스턴스
     * @param request HTTP 요청 정보
     * @return 403 상태 코드와 ErrorRes 응답
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRes> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        // SecurityContext에서 인증 정보 추출 (있으면)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        log.warn("권한 거부: user={}, path={}, error={}",
                username, request.getRequestURI(), ex.getMessage());

        ErrorRes errorResponse = ErrorRes.of(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                AUTH_ACCESS_DENIED.getCode(),
                AUTH_ACCESS_DENIED.getMessage(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }
}
