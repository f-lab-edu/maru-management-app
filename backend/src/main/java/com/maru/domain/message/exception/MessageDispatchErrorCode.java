package com.maru.domain.message.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageDispatchErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE_001", "메시지를 찾을 수 없습니다"),

    GUARDIAN_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE_101", "보호자 정보는 필수입니다"),
    TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE_102", "알림 제목은 필수입니다"),
    BODY_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE_103", "알림 내용은 필수입니다"),
    UNSUPPORTED_CHANNEL(HttpStatus.BAD_REQUEST, "MESSAGE_104", "지원하지 않는 메시지 채널입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
