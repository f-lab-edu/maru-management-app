package com.maru.service.notification;

import com.maru.domain.message.MessageQueue;
import com.maru.domain.message.MessageType;

public interface MessageSender {

    /**
     * 메시지 발송
     *
     * @param message 발송할 메시지
     */
    void send(MessageQueue message);

    /**
     * 해당 메시지 타입 지원 여부
     *
     * @param type 메시지 타입
     * @return 지원 여부
     */
    boolean supports(MessageType type);
}