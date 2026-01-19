package com.maru.service.notification;

import com.maru.domain.message.MessageQueue;

import java.util.Optional;

public interface MessageAcquirer {

    /**
     * 메시지를 선점하고 PROCESSING 상태로 전환
     *
     * @param messageId 메시지 ID
     * @return 선점 성공 시 메시지, 실패 시 empty
     */
    Optional<MessageQueue> acquire(String messageId);
}
