package com.maru.service.notification;

import com.maru.domain.message.MessageQueue;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageDispatcher {

    private final MessageQueueRepository messageQueueRepository;
    private final MessageSender messageSender;
    private final MessageAcquirer messageAcquirer;
    private final TransactionTemplate transactionTemplate;

    /**
     * 비동기 메시지 발송
     *
     * @Async는 새 스레드에서 실행되므로 ThreadLocal(TenantContext) 전파가 안 됨.
     * tenantId를 파라미터로 받아 명시적으로 설정.
     *
     * @param messageId 발송할 메시지 ID
     * @param tenantId  테넌트 ID
     */
    @Async
    public void sendAsync(Long messageId, Long tenantId) {
        try (AutoCloseable ignored = TenantContextHolder.withTenant(tenantId)) {
            trySendMessage(messageId);
        } catch (Exception e) {
            log.error("비동기 발송 중 예외 발생: messageId={}", messageId, e);
        }
    }

    private void trySendMessage(Long messageId) {
        MessageQueue message = messageAcquirer.acquire(messageId).orElse(null);
        if (message == null) {
            log.debug("메시지 선점 실패 (이미 처리 중): messageId={}", messageId);
            return;
        }

        try {
            messageSender.send(message);
            markAsSent(messageId);
            log.info("즉시 발송 성공: messageId={}", messageId);
        } catch (Exception e) {
            log.warn("즉시 발송 실패 (스케줄러가 재시도): messageId={}, error={}", messageId, e.getMessage());
            markAsPendingWithFailure(messageId, e.getMessage());
        }
    }

    /**
     * 발송 성공 기록
     */
    private void markAsSent(Long messageId) {
        transactionTemplate.executeWithoutResult(status -> {
            MessageQueue message = messageQueueRepository.findById(messageId).orElseThrow();
            message.markAsSent();
            messageQueueRepository.save(message);
        });
    }

    /**
     * 발송 실패 기록
     */
    private void markAsPendingWithFailure(Long messageId, String errorMessage) {
        transactionTemplate.executeWithoutResult(status -> {
            MessageQueue message = messageQueueRepository.findById(messageId).orElseThrow();
            message.incrementFailedCount();
            message.markAsPending();
            messageQueueRepository.save(message);
        });
    }
}