package com.maru.controller.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.maru.common.exception.DomainErrorCode;
import com.maru.common.exception.ErrorCode;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ErrorRes(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    Integer status,
    String error,
    String code,
    String message,
    String path,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, Object> data
) {
    public static ErrorRes of(ErrorCode errorCode, String path) {
        return ErrorRes.of(errorCode, path, null);
    }

    public static ErrorRes of(ErrorCode errorCode, String path, Map<String, Object> data) {
        return ErrorRes.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().name())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .data(data)
                .build();
    }

    public static ErrorRes ofDomain(DomainErrorCode errorCode, String path) {
        return ErrorRes.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .path(path)
                .build();
    }
}
