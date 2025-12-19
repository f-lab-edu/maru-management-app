package com.maru.scheduler;

import com.maru.domain.message.MessageQueue;
import com.maru.domain.message.MessageStatus;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.service.notification.MessageAcquirer;
import com.maru.service.notification.MessageSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MessageQueueRetryWorker")
class MessageQueueRetryWorkerTest {

    @Mock
    MessageQueueRepository messageQueueRepository;

    @Mock
    MessageSender messageSender;

    @Mock
    MessageAcquirer messageAcquirer;

    @InjectMocks
    MessageQueueRetryWorker worker;

    @Nested
    @DisplayName("retryFailedMessages 메서드는")
    class RetryFailedMessages {

        @Test
        @DisplayName("대상 메시지가 없으면 발송하지 않는다")
        void doesNothingWhenNoMessages() {
            // given
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(Collections.emptyList());

            // when
            worker.retryFailedMessages();

            // then
            then(messageSender).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("PENDING 메시지를 조회하여 발송한다")
        void sendsMessageWhenFound() {
            // given
            MessageQueue message = createMockMessage(1L, 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message));
            given(messageAcquirer.acquire(1L)).willReturn(Optional.of(message));

            // when
            worker.retryFailedMessages();

            // then
            then(messageAcquirer).should().acquire(1L);
            then(messageSender).should().send(message);
        }

        @Test
        @DisplayName("발송 성공 시 SENT 상태로 변경한다")
        void marksSentOnSuccess() {
            // given
            MessageQueue message = createMockMessage(1L, 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message));
            given(messageAcquirer.acquire(1L)).willReturn(Optional.of(message));

            // when
            worker.retryFailedMessages();

            // then
            then(message).should().markAsSent();
        }

        @Test
        @DisplayName("발송 실패 시 failedCount를 증가시킨다")
        void incrementsFailedCountOnFailure() {
            // given
            MessageQueue message = createMockMessage(1L, 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message));
            given(messageAcquirer.acquire(1L)).willReturn(Optional.of(message));
            doThrow(new RuntimeException("발송 실패")).when(messageSender).send(message);

            // when
            worker.retryFailedMessages();

            // then
            then(message).should().incrementFailedCount();
            then(message).should().markAsPending();
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 시 FAILED 상태로 변경한다")
        void marksFailedWhenMaxRetryExceeded() {
            // given
            MessageQueue message = createMockMessage(1L, 2);
            given(message.getFailedCount()).willReturn(2).willReturn(3);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message));
            given(messageAcquirer.acquire(1L)).willReturn(Optional.of(message));
            doThrow(new RuntimeException("발송 실패")).when(messageSender).send(message);

            // when
            worker.retryFailedMessages();

            // then
            then(message).should().markAsFailed("발송 실패");
        }

        @Test
        @DisplayName("이미 다른 스레드가 선점한 메시지는 스킵한다")
        void skipsAlreadyAcquiredMessage() {
            // given
            MessageQueue message = createMockMessage(1L, 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message));
            given(messageAcquirer.acquire(1L)).willReturn(Optional.empty());

            // when
            worker.retryFailedMessages();

            // then
            then(messageSender).shouldHaveNoInteractions();
        }
    }

    private MessageQueue createMockMessage(Long id, int failedCount) {
        MessageQueue message = mock(MessageQueue.class);
        given(message.getId()).willReturn(id);
        given(message.getFailedCount()).willReturn(failedCount);
        return message;
    }
}