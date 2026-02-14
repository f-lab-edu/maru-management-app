package com.maru.domain.message.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageBroadcastErrorCode implements BaseErrorCode {

    NO_RECIPIENTS(HttpStatus.BAD_REQUEST, "BROADCAST_001", "발송 대상이 없습니다"),
    EMPTY_BODY(HttpStatus.BAD_REQUEST, "BROADCAST_002", "메시지 본문이 비어있습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "BROADCAST_003", "발송 내역을 찾을 수 없습니다"),
    INVALID_RECIPIENT_TYPE(HttpStatus.BAD_REQUEST, "BROADCAST_004", "수신자 유형이 올바르지 않습니다"),
    UNSUPPORTED_CHANNEL(HttpStatus.BAD_REQUEST, "BROADCAST_005", "지원하지 않는 발송 채널입니다"),
    EMPTY_TITLE(HttpStatus.BAD_REQUEST, "BROADCAST_006", "메시지 제목이 비어있습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
