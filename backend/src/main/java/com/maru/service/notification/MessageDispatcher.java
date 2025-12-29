package com.maru.service.notification;

import com.maru.domain.message.MessageQueue;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.security.TenantContextHolder;
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
     * @Async는 새 스레드에서 실행되므로 ThreadLocal(TenantContext) 전파가 안 됨.
     * tenantId를 파라미터로 받아 명시적으로 설정.
     *
     * @param messageIds 발송할 메시지 ID 목록
     * @param tenantId   테넌트 ID
     */
    @Async
    public void sendBatchAsync(List<Long> messageIds, Long tenantId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        try (AutoCloseable ignored = TenantContextHolder.withTenant(tenantId)) {
            trySendBatch(messageIds);
        } catch (Exception e) {
            log.error("배치 발송 중 예외 발생: messageCount={}", messageIds.size(), e);
        }
    }

    private void trySendBatch(List<Long> messageIds) {
        List<MessageQueue> acquiredMessages = new ArrayList<>();

        for (Long messageId : messageIds) {
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
