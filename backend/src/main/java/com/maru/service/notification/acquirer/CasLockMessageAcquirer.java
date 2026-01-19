package com.maru.service.notification.acquirer;

import com.maru.domain.message.MessageQueue;
import com.maru.domain.message.MessageStatus;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.service.notification.MessageAcquirer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

/**
 * Compare And Swap 알고리즘 기반 메시지 선점 (멀티 스레드 환경에서 중복 발송 방지)
 */
@Primary
@Component
@RequiredArgsConstructor
public class CasLockMessageAcquirer implements MessageAcquirer {

    private final MessageQueueRepository messageQueueRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Optional<MessageQueue> acquire(String messageId) {
        return Optional.ofNullable(transactionTemplate.execute(status -> {
            int updated = messageQueueRepository.tryTransitionStatus(
                    messageId,
                    MessageStatus.PENDING,
                    MessageStatus.PROCESSING
            );

            if (updated == 0) {
                return null;
            }

            return messageQueueRepository.findById(messageId).orElse(null);
        }));
    }
}