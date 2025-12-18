package com.maru.service.notification;

import com.maru.domain.message.event.MessageReadyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MessageDispatchListener {

    private final AsyncNotificationSender asyncNotificationSender;

    /**
     * 메시지 저장 커밋 후 비동기 발송 트리거
     *
     * @param event 메시지 준비 완료 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageReady(MessageReadyEvent event) {
        asyncNotificationSender.sendAsync(event.messageId(), event.tenantId());
    }
}