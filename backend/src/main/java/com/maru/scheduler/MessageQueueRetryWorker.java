package com.maru.scheduler;

import com.maru.domain.message.MessageQueue;
import com.maru.domain.message.MessageStatus;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.service.notification.MessageAcquirer;
import com.maru.service.notification.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageQueueRetryWorker {

    private static final int MAX_RETRY = 3;
    private static final int BATCH_SIZE = 100;
    private static final int STUCK_THRESHOLD_MINUTES = 10;

    private final MessageQueueRepository messageQueueRepository;
    private final MessageSender messageSender;
    private final MessageAcquirer messageAcquirer;
    private final TransactionTemplate transactionTemplate;

    /**
     * 즉시 발송 실패 건 재시도 (10초마다)
     */
    @Scheduled(fixedDelay = 10000)
    public void retryFailedMessages() {
        List<MessageQueue> messages = messageQueueRepository.findRetryTargets(
                MessageStatus.PENDING,
                LocalDateTime.now(),
                MAX_RETRY,
                PageRequest.of(0, BATCH_SIZE)
        );

        if (messages.isEmpty()) {
            return;
        }

        log.info("재시도 대상 메시지: {}건", messages.size());
        processBatchRetry(messages);
    }

    private void processBatchRetry(List<MessageQueue> messages) {
        List<MessageQueue> acquiredMessages = new ArrayList<>();

        for (MessageQueue message : messages) {
            messageAcquirer.acquire(message.getId()).ifPresent(acquiredMessages::add);
        }

        if (acquiredMessages.isEmpty()) {
            log.debug("선점된 메시지 없음");
            return;
        }

        try {
            messageSender.sendBatch(acquiredMessages);
            markAllAsSent(acquiredMessages);
            log.info("재시도 배치 발송 성공: {}건", acquiredMessages.size());
        } catch (Exception e) {
            log.warn("재시도 배치 발송 실패: {}건, error={}", acquiredMessages.size(), e.getMessage());
            handleBatchSendFailure(acquiredMessages, e.getMessage());
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

    private void handleBatchSendFailure(List<MessageQueue> messages, String errorMessage) {
        transactionTemplate.executeWithoutResult(status -> {
            for (MessageQueue message : messages) {
                message.incrementFailedCount();
                if (message.getFailedCount() >= MAX_RETRY) {
                    message.markAsFailed(errorMessage);
                    log.error("최종 발송 실패 (재시도 횟수 초과): messageId={}", message.getId());
                } else {
                    message.markAsPending();
                }
            }
            messageQueueRepository.saveAll(messages);
        });
    }

    /**
     * PROCESSING 상태로 오래 방치된 메시지 복구 (1분마다)
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void recoverStuckMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(STUCK_THRESHOLD_MINUTES);
        int recovered = messageQueueRepository.recoverStuckProcessing(threshold);

        if (recovered > 0) {
            log.warn("PROCESSING 상태 복구: {}건 ({}분 이상 방치)", recovered, STUCK_THRESHOLD_MINUTES);
        }
    }
}
