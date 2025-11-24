package com.maru.controller.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * API 에러 응답 DTO
 */
@Builder
public record ErrorRes(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    Integer status,
    String error,
    String code,
    String message,
    String detail,
    String path
) {
    /**
     * ErrorRes 생성 (detail 없음)
     *
     * @param status HTTP 상태 코드
     * @param error HTTP 상태 이름
     * @param code 애플리케이션 에러 코드
     * @param message 사용자 대면 메시지
     * @param path 요청 URI
     * @return ErrorRes 인스턴스
     */
    public static ErrorRes of(int status, String error, String code, String message, String path) {
        return ErrorRes.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .code(code)
                .message(message)
                .path(path)
                .build();
    }

    /**
     * ErrorRes 생성 (detail 포함)
     *
     * @param status HTTP 상태 코드
     * @param error HTTP 상태 이름
     * @param code 애플리케이션 에러 코드
     * @param message 사용자 대면 메시지
     * @param detail 상세 에러 설명
     * @param path 요청 URI
     * @return ErrorRes 인스턴스
     */
    public static ErrorRes of(int status, String error, String code, String message, String detail, String path) {
        return ErrorRes.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .code(code)
                .message(message)
                .detail(detail)
                .path(path)
                .build();
    }
}
