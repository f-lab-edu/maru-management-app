package com.maru.controller.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.maru.common.exception.ErrorCode;
import lombok.Builder;

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
}
