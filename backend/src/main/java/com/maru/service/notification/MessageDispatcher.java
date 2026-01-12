package com.maru.service.notification;

import com.maru.domain.message.MessageQueue;
import com.maru.repository.message.MessageQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageDispatcher {

    private final MessageQueueRepository messageQueueRepository;
    private final MessageSender messageSender;
    private final MessageAcquirer messageAcquirer;
    private final TransactionTemplate transactionTemplate;

    /**
     * 비동기 메시지 배치 발송
     *
     * @param messageIds 발송할 메시지 ID 목록
     */
    @Async
    public void sendBatchAsync(List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        trySendBatch(messageIds);
    }

    private void trySendBatch(List<String> messageIds) {
        List<MessageQueue> acquiredMessages = new ArrayList<>();

        for (String messageId : messageIds) {
            messageAcquirer.acquire(messageId).ifPresent(acquiredMessages::add);
        }

        if (acquiredMessages.isEmpty()) {
            log.debug("선점된 메시지 없음");
            return;
        }

        try {
            messageSender.sendBatch(acquiredMessages);
            markAllAsSent(acquiredMessages);
            log.info("배치 발송 성공: {}건", acquiredMessages.size());
        } catch (Exception e) {
            log.warn("배치 발송 실패 (스케줄러가 재시도): {}건, error={}", acquiredMessages.size(), e.getMessage());
            markAllAsPendingWithFailure(acquiredMessages, e.getMessage());
        }
    }

    private void markAllAsSent(List<MessageQueue> messages) {
        transactionTemplate.executeWithoutResult(status -> {
            for (MessageQueue message : messages) {
                message.markAsSent();
            }
            messageQueueRepository.saveAll(messages);
        });
    }

    private void markAllAsPendingWithFailure(List<MessageQueue> messages, String errorMessage) {
        transactionTemplate.executeWithoutResult(status -> {
            for (MessageQueue message : messages) {
                message.incrementFailedCount();
                message.markAsPending();
            }
            messageQueueRepository.saveAll(messages);
        });
    }
}
