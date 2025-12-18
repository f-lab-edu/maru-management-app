package com.maru.service.notification;

import com.maru.domain.message.MessageQueue;

public interface MessageSender {

    /**
     * 메시지 발송
     *
     * @param message 발송할 메시지
     */
    void send(MessageQueue message);
}