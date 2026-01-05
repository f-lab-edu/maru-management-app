package com.maru.service.notification;

import com.maru.domain.message.MessageQueue;

import java.util.List;

public interface MessageSender {

    /**
     * 메시지 배치 발송
     *
     * @param messages 발송할 메시지 목록
     */
    void sendBatch(List<MessageQueue> messages);
}
