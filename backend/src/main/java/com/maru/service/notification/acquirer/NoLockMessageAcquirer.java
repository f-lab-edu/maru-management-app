package com.maru.service.notification.acquirer;

import com.maru.domain.message.MessageQueue;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.service.notification.MessageAcquirer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

/**
 * 락을 사용하지 않는 메시지 선점
 */
@Primary
@Component
@RequiredArgsConstructor
public class NoLockMessageAcquirer implements MessageAcquirer {

    private final MessageQueueRepository messageQueueRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Optional<MessageQueue> acquire(String messageId) {
        return Optional.ofNullable(transactionTemplate.execute(status -> {
            MessageQueue message = messageQueueRepository.findByIdWithGuardian(messageId).orElse(null);
            if (message != null) {
                message.markAsProcessing();
            }
            return message;
        }));
    }
}