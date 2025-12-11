package com.maru.common.exception;

import com.maru.controller.common.ErrorRes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.http.HttpStatus;

import java.util.Map;

import static com.maru.common.exception.ErrorCode.*;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SmsVerificationException.class)
    public ResponseEntity<ErrorRes> handleSmsVerificationException(
            SmsVerificationException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();

        log.warn("SMS 인증 예외: code={}, remainingAttempts={}, path={}",
                errorCode.getCode(), ex.getRemainingAttempts(), request.getRequestURI());

        Map<String, Object> data = Map.of("remainingAttempts", ex.getRemainingAttempts());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorRes.of(errorCode, request.getRequestURI(), data));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorRes> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();

        log.warn("비즈니스 예외: code={}, status={}, path={}",
                errorCode.getCode(), errorCode.getStatus().value(), request.getRequestURI());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorRes.of(errorCode, request.getRequestURI()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorRes> handleDomainException(
            DomainException ex,
            HttpServletRequest request) {

        DomainErrorCode errorCode = ex.getErrorCode();

        log.warn("도메인 예외: code={}, path={}", errorCode.name(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorRes.ofDomain(errorCode, request.getRequestURI()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorRes> handleAuthException(
            AuthException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();

        log.warn("인증 예외: code={}, status={}, path={}",
                errorCode.getCode(), errorCode.getStatus().value(), request.getRequestURI());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorRes.of(errorCode, request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRes> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("인증 실패: path={}, error={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(AUTH_REQUIRED.getStatus())
                .body(ErrorRes.of(AUTH_REQUIRED, request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRes> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("권한 거부: path={}, error={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(AUTH_ACCESS_DENIED.getStatus())
                .body(ErrorRes.of(AUTH_ACCESS_DENIED, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("서버 내부 오류: path={}", request.getRequestURI(), ex);

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorRes.of(INTERNAL_SERVER_ERROR, request.getRequestURI()));
    }
}
