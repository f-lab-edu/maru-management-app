package com.maru.domain.message.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageQueueErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE_001", "메시지를 찾을 수 없습니다"),

    GUARDIAN_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE_101", "guardian은 필수입니다"),
    STUDENT_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE_102", "student는 필수입니다"),
    TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE_103", "title은 필수입니다"),
    BODY_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE_104", "body는 필수입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}