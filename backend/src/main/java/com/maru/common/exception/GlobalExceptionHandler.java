package com.maru.common.exception;

import com.maru.common.exception.auth.AuthErrorCode;
import com.maru.common.exception.auth.AuthException;
import com.maru.common.exception.sms.SmsVerificationException;
import com.maru.controller.common.ErrorRes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SmsVerificationException.class)
    public ResponseEntity<ErrorRes> handleSmsVerificationException(
            SmsVerificationException ex,
            HttpServletRequest request) {

        BaseErrorCode errorCode = ex.getErrorCode();

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

        BaseErrorCode errorCode = ex.getErrorCode();

        log.warn("비즈니스 예외: code={}, status={}, path={}",
                errorCode.getCode(), errorCode.getStatus().value(), request.getRequestURI());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorRes.of(errorCode, request.getRequestURI()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorRes> handleAuthException(
            AuthException ex,
            HttpServletRequest request) {

        BaseErrorCode errorCode = ex.getErrorCode();

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
                .status(AuthErrorCode.REQUIRED.getStatus())
                .body(ErrorRes.of(AuthErrorCode.REQUIRED, request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRes> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("권한 거부: path={}, error={}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(AuthErrorCode.ACCESS_DENIED.getStatus())
                .body(ErrorRes.of(AuthErrorCode.ACCESS_DENIED, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("서버 내부 오류: path={}", request.getRequestURI(), ex);

        return ResponseEntity
                .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorRes.of(CommonErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI()));
    }
}
