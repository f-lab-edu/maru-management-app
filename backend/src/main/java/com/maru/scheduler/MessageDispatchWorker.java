package com.maru.scheduler;

import com.maru.config.properties.MessageWorkerProperties;
import com.maru.domain.message.MessageDispatchAttempt;
import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.MessageDispatch;
import com.maru.repository.message.MessageDispatchAttemptRepository;
import com.maru.repository.message.MessageDispatchRepository;
import com.maru.service.notification.adapter.MessageChannelAdapter;
import com.maru.service.notification.adapter.MessageChannelAdapterFactory;
import com.maru.service.notification.result.BatchSendResult;
import com.maru.service.notification.result.SendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageDispatchWorker {

    private static final String INSTANCE_ID = generateInstanceId();
    private static final String ERROR_BODY_MISSING = "BODY_MISSING";

    private final MessageDispatchRepository messageDispatchRepository;
    private final MessageDispatchAttemptRepository attemptRepository;
    private final MessageChannelAdapterFactory adapterFactory;
    private final TransactionTemplate transactionTemplate;
    private final MessageWorkerProperties properties;

    /**
     * PENDING 메시지를 배치로 선점하여 채널별로 발송 처리
     */
    @Scheduled(fixedDelayString = "${message.worker.poll-delay-ms:3000}")
    public void processMessages() {
        LocalDateTime claimTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<MessageDispatch> messages = claimMessages(claimTime);

        if (messages.isEmpty()) {
            return;
        }

        log.info("메시지 선점: {}건", messages.size());

        List<MessageDispatch> validMessages = new ArrayList<>();
        List<MessageDispatch> invalidMessages = new ArrayList<>();
        List<MessageDispatchAttempt> invalidAttempts = new ArrayList<>();

        for (MessageDispatch msg : messages) {
            if (msg.getBody() != null) {
                validMessages.add(msg);
            } else {
                log.warn("본문 없는 메시지 (DEAD 처리): dispatchId={}", msg.getId());
                MessageDispatchAttempt attempt = MessageDispatchAttempt.start(
                        msg.getId(), msg.getFailedCount() + 1, msg.getChannel(), INSTANCE_ID);
                msg.markAsDead("메시지 본문이 없습니다");
                attempt.complete(false, null, ERROR_BODY_MISSING, "메시지 본문이 없습니다");
                invalidMessages.add(msg);
                invalidAttempts.add(attempt);
            }
        }

        if (!invalidMessages.isEmpty()) {
            transactionTemplate.executeWithoutResult(status -> {
                messageDispatchRepository.saveAll(invalidMessages);
                attemptRepository.saveAll(invalidAttempts);
            });
            log.warn("본문 없는 메시지 DEAD 처리: {}건", invalidMessages.size());
        }

        if (validMessages.isEmpty()) {
            return;
        }

        Map<MessageChannel, List<MessageDispatch>> byChannel = validMessages.stream()
                .collect(Collectors.groupingBy(MessageDispatch::getChannel));

        for (var entry : byChannel.entrySet()) {
            processChannelBatch(entry.getKey(), entry.getValue());
        }
    }

    private List<MessageDispatch> claimMessages(LocalDateTime claimTime) {
        return transactionTemplate.execute(status -> {
            int claimed = messageDispatchRepository.claimBatch(
                    INSTANCE_ID, claimTime, properties.maxRetry(), properties.batchSize());
            if (claimed == 0) return List.of();
            return messageDispatchRepository.findClaimedMessages(INSTANCE_ID, claimTime);
        });
    }

    private void processChannelBatch(MessageChannel channel, List<MessageDispatch> messages) {
        List<MessageDispatchAttempt> attempts = messages.stream()
                .map(msg -> MessageDispatchAttempt.start(
                        msg.getId(), msg.getFailedCount() + 1, channel, INSTANCE_ID))
                .toList();

        MessageChannelAdapter adapter = adapterFactory.getAdapter(channel);
        BatchSendResult result = adapter.sendBatch(messages);

        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < messages.size(); i++) {
                MessageDispatch msg = messages.get(i);
                MessageDispatchAttempt attempt = attempts.get(i);
                SendResult sendResult = result.results().get(msg.getId());

                if (sendResult != null && sendResult.success()) {
                    msg.markAsAccepted(sendResult.vendorMessageId());
                    attempt.complete(true, sendResult.vendorMessageId(), null, null);
                } else {
                    String errorCode = sendResult != null ? sendResult.errorCode() : "UNKNOWN";
                    String errorMsg = sendResult != null ? sendResult.errorMessage() : "응답 없음";
                    msg.incrementFailedCount();
                    attempt.complete(false, null, errorCode, errorMsg);

                    if (msg.getFailedCount() >= properties.maxRetry()) {
                        msg.markAsDead(errorMsg);
                        log.error("최종 발송 실패: dispatchId={}", msg.getId());
                    } else {
                        msg.scheduleRetry(errorMsg);
                    }
                }
            }
            messageDispatchRepository.saveAll(messages);
            attemptRepository.saveAll(attempts);
        });

        log.info("채널={} 처리 완료: 성공={}, 실패={}",
                channel, result.getSuccessIds().size(), result.getFailureIds().size());
    }

    /**
     * PROCESSING 상태로 오래 멈춘 메시지를 PENDING으로 복구
     */
    @Scheduled(fixedDelay = 60000)
    public void recoverStuckMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(properties.stuckThresholdMinutes());
        Integer recovered = transactionTemplate.execute(status ->
                messageDispatchRepository.recoverStuckProcessing(threshold));
        if (recovered != null && recovered > 0) {
            log.warn("stuck 메시지 복구: {}건", recovered);
        }
    }

    private static String generateInstanceId() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            long pid = ProcessHandle.current().pid();
            return String.format("%s-%d-%s", hostname, pid,
                    UUID.randomUUID().toString().substring(0, 8));
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
