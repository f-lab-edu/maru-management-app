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
    UNSUPPORTED_CHANNEL(HttpStatus.BAD_REQUEST, "MESSAGE_104", "지원하지 않는 메시지 채널입니다"),

    // 비즈니스 규칙 위반 (상태 전이)
    CANNOT_DEAD_FROM_ACCEPTED(HttpStatus.BAD_REQUEST, "MESSAGE_301", "발송 완료된 메시지는 실패 처리할 수 없습니다"),
    CANNOT_RETRY_TERMINAL_STATUS(HttpStatus.BAD_REQUEST, "MESSAGE_302", "최종 상태의 메시지는 재시도할 수 없습니다"),
    CANNOT_RESEND_NON_DEAD(HttpStatus.BAD_REQUEST, "MESSAGE_303", "최종 실패 상태의 메시지만 재발송할 수 있습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
