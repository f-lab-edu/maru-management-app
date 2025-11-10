package com.maru.common.exception;

import com.maru.controller.common.ErrorRes;
import jakarta.persistence.EntityNotFoundException;
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

import static com.maru.common.exception.ErrorCode.*;


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

    /**
     * IllegalArgumentException 처리 (400 Bad Request)
     *
     * @param ex IllegalArgumentException 인스턴스
     * @param request HTTP 요청 정보
     * @return 400 상태 코드와 ErrorRes 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRes> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.error("잘못된 요청 파라미터: path={}, error={}",
                request.getRequestURI(), ex.getMessage());

        ErrorRes errorResponse = ErrorRes.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                BAD_REQUEST.getCode(),
                BAD_REQUEST.getMessage(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * EntityNotFoundException 처리 (404 Not Found)
     *
     * @param ex EntityNotFoundException 인스턴스
     * @param request HTTP 요청 정보
     * @return 404 상태 코드와 ErrorRes 응답
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorRes> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        log.warn("엔티티를 찾을 수 없음: path={}, error={}",
                request.getRequestURI(), ex.getMessage());

        ErrorRes errorResponse = ErrorRes.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                NOT_FOUND.getCode(),
                NOT_FOUND.getMessage(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * 모든 예외의 최종 핸들러 (500 Internal Server Error)
     *
     * @param ex Exception 인스턴스
     * @param request HTTP 요청 정보
     * @return 500 상태 코드와 ErrorRes 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("서버 내부 오류: path={}, error={}",
                request.getRequestURI(), ex.getMessage(), ex);

        ErrorRes errorResponse = ErrorRes.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                INTERNAL_SERVER_ERROR.getCode(),
                INTERNAL_SERVER_ERROR.getMessage(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
