package com.maru.scheduler;

import com.maru.domain.message.MessageQueue;
import com.maru.domain.message.MessageStatus;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.service.notification.MessageAcquirer;
import com.maru.service.notification.MessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @Mock
    TransactionTemplate transactionTemplate;

    @Captor
    ArgumentCaptor<List<MessageQueue>> messageListCaptor;

    MessageQueueRetryWorker worker;

    @BeforeEach
    void setUp() {
        worker = new MessageQueueRetryWorker(
                messageQueueRepository,
                messageSender,
                messageAcquirer,
                transactionTemplate
        );

        doAnswer(invocation -> {
            Consumer<Object> action = invocation.getArgument(0);
            action.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

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
        @DisplayName("PENDING 메시지를 조회하여 배치 발송한다")
        void sendsBatchWhenFound() {
            // given
            MessageQueue message1 = createMockMessage("msg1", 0);
            MessageQueue message2 = createMockMessage("msg2", 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message1, message2));
            given(messageAcquirer.acquire("msg1")).willReturn(Optional.of(message1));
            given(messageAcquirer.acquire("msg2")).willReturn(Optional.of(message2));

            // when
            worker.retryFailedMessages();

            // then
            verify(messageSender).sendBatch(messageListCaptor.capture());
            assertThat(messageListCaptor.getValue()).containsExactly(message1, message2);
        }

        @Test
        @DisplayName("발송 성공 시 모든 메시지를 SENT 상태로 변경한다")
        void marksSentOnSuccess() {
            // given
            MessageQueue message1 = createMockMessage("msg1", 0);
            MessageQueue message2 = createMockMessage("msg2", 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message1, message2));
            given(messageAcquirer.acquire("msg1")).willReturn(Optional.of(message1));
            given(messageAcquirer.acquire("msg2")).willReturn(Optional.of(message2));

            // when
            worker.retryFailedMessages();

            // then
            then(message1).should().markAsSent();
            then(message2).should().markAsSent();
            verify(messageQueueRepository).saveAll(any());
        }

        @Test
        @DisplayName("배치 발송 실패 시 모든 메시지의 failedCount를 증가시킨다")
        void incrementsFailedCountOnBatchFailure() {
            // given
            MessageQueue message1 = createMockMessage("msg1", 0);
            MessageQueue message2 = createMockMessage("msg2", 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message1, message2));
            given(messageAcquirer.acquire("msg1")).willReturn(Optional.of(message1));
            given(messageAcquirer.acquire("msg2")).willReturn(Optional.of(message2));
            doThrow(new RuntimeException("발송 실패")).when(messageSender).sendBatch(any());

            // when
            worker.retryFailedMessages();

            // then
            then(message1).should().incrementFailedCount();
            then(message2).should().incrementFailedCount();
            then(message1).should().markAsPending();
            then(message2).should().markAsPending();
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 시 FAILED 상태로 변경한다")
        void marksFailedWhenMaxRetryExceeded() {
            // given
            MessageQueue message = createMockMessage("msg1", 2);
            given(message.getFailedCount()).willReturn(3);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message));
            given(messageAcquirer.acquire("msg1")).willReturn(Optional.of(message));
            doThrow(new RuntimeException("발송 실패")).when(messageSender).sendBatch(any());

            // when
            worker.retryFailedMessages();

            // then
            then(message).should().markAsFailed("발송 실패");
        }

        @Test
        @DisplayName("이미 다른 스레드가 선점한 메시지는 배치에서 제외된다")
        void skipsAlreadyAcquiredMessage() {
            // given
            MessageQueue message1 = createMockMessage("msg1", 0);
            MessageQueue message2 = createMockMessage("msg2", 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message1, message2));
            given(messageAcquirer.acquire("msg1")).willReturn(Optional.of(message1));
            given(messageAcquirer.acquire("msg2")).willReturn(Optional.empty());

            // when
            worker.retryFailedMessages();

            // then
            verify(messageSender).sendBatch(messageListCaptor.capture());
            assertThat(messageListCaptor.getValue()).containsExactly(message1);
        }

        @Test
        @DisplayName("모든 메시지가 선점 실패하면 발송하지 않는다")
        void doesNothingWhenAllAcquireFailed() {
            // given
            MessageQueue message1 = createMockMessage("msg1", 0);
            MessageQueue message2 = createMockMessage("msg2", 0);
            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message1, message2));
            given(messageAcquirer.acquire("msg1")).willReturn(Optional.empty());
            given(messageAcquirer.acquire("msg2")).willReturn(Optional.empty());

            // when
            worker.retryFailedMessages();

            // then
            then(messageSender).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("배치 처리 테스트")
    class BatchProcessingTest {

        @Test
        @DisplayName("다수의 메시지를 한 번의 배치 호출로 처리한다")
        void processesManyMessagesInSingleBatch() {
            // given
            int messageCount = 50;
            List<MessageQueue> messages = IntStream.range(0, messageCount)
                    .mapToObj(i -> createMockMessage("msg" + i, 0))
                    .toList();

            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(messages);

            for (int i = 0; i < messageCount; i++) {
                given(messageAcquirer.acquire("msg" + i)).willReturn(Optional.of(messages.get(i)));
            }

            // when
            worker.retryFailedMessages();

            // then
            verify(messageSender, times(1)).sendBatch(messageListCaptor.capture());
            assertThat(messageListCaptor.getValue()).hasSize(messageCount);
        }

        @Test
        @DisplayName("배치 발송 실패 시 MAX_RETRY 기준으로 각 메시지 상태를 결정한다")
        void handlesMixedRetryCountsOnBatchFailure() {
            // given
            MessageQueue message1 = createMockMessage("msg1", 0);
            MessageQueue message2 = createMockMessage("msg2", 2);
            MessageQueue message3 = createMockMessage("msg3", 1);

            given(message1.getFailedCount()).willReturn(1);
            given(message2.getFailedCount()).willReturn(3);
            given(message3.getFailedCount()).willReturn(2);

            given(messageQueueRepository.findRetryTargets(
                    eq(MessageStatus.PENDING),
                    any(LocalDateTime.class),
                    eq(3),
                    any(PageRequest.class)
            )).willReturn(List.of(message1, message2, message3));

            given(messageAcquirer.acquire("msg1")).willReturn(Optional.of(message1));
            given(messageAcquirer.acquire("msg2")).willReturn(Optional.of(message2));
            given(messageAcquirer.acquire("msg3")).willReturn(Optional.of(message3));

            doThrow(new RuntimeException("발송 실패")).when(messageSender).sendBatch(any());

            // when
            worker.retryFailedMessages();

            // then
            then(message1).should().markAsPending();
            then(message2).should().markAsFailed("발송 실패");
            then(message3).should().markAsPending();
        }
    }

    private MessageQueue createMockMessage(String id, int failedCount) {
        MessageQueue message = mock(MessageQueue.class);
        given(message.getId()).willReturn(id);
        given(message.getFailedCount()).willReturn(failedCount);
        return message;
    }
}
