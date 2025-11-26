package com.maru.common.exception;

import com.maru.controller.common.ErrorRes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.maru.common.exception.ErrorCode.*;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorRes> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = errorCode.getStatus();

        log.warn("비즈니스 예외: code={}, status={}, path={}",
                errorCode.getCode(), status.value(), request.getRequestURI());

        return buildErrorResponse(status, errorCode, ex.getMessage(), request);
    }

    /**
     * AuthException 처리
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorRes> handleAuthException(
            AuthException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = errorCode.getStatus();

        log.warn("인증 예외: code={}, status={}, path={}",
                errorCode.getCode(), status.value(), request.getRequestURI());

        return buildErrorResponse(status, errorCode, ex.getMessage(), request);
    }

    /**
     * Spring Security AuthenticationException 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRes> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("인증 실패: path={}, error={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, AUTH_REQUIRED, ex.getMessage(), request);
    }

    /**
     * Spring Security AccessDeniedException 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRes> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("권한 거부: path={}, error={}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(HttpStatus.FORBIDDEN, AUTH_ACCESS_DENIED, ex.getMessage(), request);
    }

    /**
     * 모든 예외의 최종 핸들러
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("서버 내부 오류: path={}", request.getRequestURI(), ex);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorRes> buildErrorResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String detail,
            HttpServletRequest request) {

        ErrorRes errorResponse = ErrorRes.of(
                status.value(),
                status.getReasonPhrase(),
                errorCode.getCode(),
                errorCode.getMessage(),
                detail,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}
