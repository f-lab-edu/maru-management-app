package com.maru.scheduler;

import com.maru.domain.message.MessageQueue;
import com.maru.domain.message.MessageStatus;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.service.notification.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageQueueRetryWorker {

    private static final int MAX_RETRY = 3;
    private static final int BATCH_SIZE = 100;

    private final MessageQueueRepository messageQueueRepository;
    private final MessageSender messageSender;

    /**
     * 즉시 발송 실패 건 재시도 (10초마다)
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
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

        for (MessageQueue message : messages) {
            processRetry(message);
        }
    }

    private void processRetry(MessageQueue message) {
        try {
            int updated = messageQueueRepository.tryTransitionStatus(
                    message.getId(),
                    MessageStatus.PENDING,
                    MessageStatus.PROCESSING
            );
            if (updated == 0) {
                return;
            }

            messageSender.send(message);
            message.markAsSent();
            log.info("재시도 발송 성공: messageId={}", message.getId());
        } catch (Exception e) {
            log.warn("재시도 발송 실패: messageId={}, failedCount={}",
                    message.getId(), message.getFailedCount() + 1);
            message.incrementFailedCount();
            if (message.getFailedCount() >= MAX_RETRY) {
                message.markAsFailed(e.getMessage());
                log.error("최종 발송 실패 (재시도 횟수 초과): messageId={}", message.getId());
            } else {
                message.markAsPending();
            }
        }
    }
}