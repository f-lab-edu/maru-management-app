package com.maru.benchmark;

import com.maru.config.properties.MessageWorkerProperties;
import com.maru.domain.message.*;
import com.maru.repository.message.MessageDispatchAttemptRepository;
import com.maru.repository.message.MessageDispatchRepository;
import com.maru.scheduler.MessageDispatchWorker;
import com.maru.service.notification.adapter.MessageChannelAdapter;
import com.maru.service.notification.adapter.MessageChannelAdapterFactory;
import com.maru.service.notification.result.BatchSendResult;
import com.maru.service.notification.result.SendResult;
import com.maru.support.TestcontainersConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 메시지 디스패처 성능 벤치마크
 *
 * <p>Docker MySQL 8.0 (Testcontainers) 환경에서 System.nanoTime() 기반으로 측정.
 * JMH 벤치마크가 아니므로 GC pause, JIT 컴파일 등의 노이즈가 포함될 수 있으나,
 * 방식 간 상대적 차이 확인이 목적.</p>
 */
@SpringBootTest
@Import({TestcontainersConfig.class, MessageDispatchBenchmarkTest.MockAdapterConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // Spring 관리 @Scheduled 워커가 벤치마크에 간섭하지 않도록 폴링 주기를 비활성화 수준으로 설정
        "message.worker.poll-delay-ms=3600000",
        "message.worker.batch-size=500",
        "message.worker.max-retry=5",
        "message.worker.stuck-threshold-minutes=10"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageDispatchBenchmarkTest {

    @Autowired
    private MessageDispatchRepository messageDispatchRepository;

    @Autowired
    private MessageDispatchAttemptRepository attemptRepository;

    @Autowired
    private MessageChannelAdapterFactory adapterFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 즉시 성공을 반환하는 Mock 어댑터 — 순수 DB 계층 성능만 측정하기 위함
     */
    @TestConfiguration
    static class MockAdapterConfig {
        @Bean
        @Primary
        MessageChannelAdapterFactory mockAdapterFactory() {
            MessageChannelAdapter mockSmsAdapter = new MessageChannelAdapter() {
                @Override
                public MessageChannel getChannel() {
                    return MessageChannel.SMS;
                }

                @Override
                public BatchSendResult sendBatch(List<MessageDispatch> messages) {
                    Map<String, SendResult> results = new HashMap<>();
                    for (MessageDispatch msg : messages) {
                        results.put(msg.getId(), SendResult.success(msg.getId(), "VENDOR_" + msg.getId()));
                    }
                    return new BatchSendResult(results);
                }
            };
            return new MessageChannelAdapterFactory(List.of(mockSmsAdapter));
        }
    }

    @BeforeEach
    void cleanUp() {
        // JDBC TRUNCATE로 JPA 캐시 오염 없이 확실하게 초기화
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE message_dispatch_attempt");
        jdbcTemplate.execute("TRUNCATE TABLE message_dispatch");
    }

    @AfterEach
    void restoreFkChecks() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    // ========== 1. claimBatch 쿼리 성능 ==========

    @Test
    @Order(1)
    @DisplayName("claimBatch 쿼리 성능 - 데이터 규모별")
    void claimBatch_쿼리_성능_측정() {
        int[] dataSizes = {1_000, 5_000, 10_000, 50_000};

        System.out.println("=== claimBatch 쿼리 성능 측정 ===");
        System.out.printf("%-12s | %-15s | %-15s | %-12s%n", "데이터 규모", "선점 시간(ms)", "조회 시간(ms)", "선점 건수");
        System.out.println("-".repeat(65));

        for (int size : dataSizes) {
            cleanUp();
            bulkInsertMessages(size);

            LocalDateTime claimTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            String owner = "bench-worker-" + UUID.randomUUID().toString().substring(0, 8);
            int batchSize = 500;

            // claimBatch UPDATE 성능
            long claimStart = System.nanoTime();
            int claimed = transactionTemplate.execute(status ->
                    messageDispatchRepository.claimBatch(owner, claimTime, 5, batchSize));
            long claimElapsed = (System.nanoTime() - claimStart) / 1_000_000;

            // findClaimedMessages SELECT 성능
            long selectStart = System.nanoTime();
            List<MessageDispatch> messages = messageDispatchRepository.findClaimedMessages(owner, claimTime);
            long selectElapsed = (System.nanoTime() - selectStart) / 1_000_000;

            System.out.printf("%-12s | %-15s | %-15s | %-12s%n",
                    String.format("%,d건", size),
                    claimElapsed + "ms",
                    selectElapsed + "ms",
                    claimed + "건");

            assertThat(claimed).isEqualTo(batchSize);
            assertThat(messages).hasSize(batchSize);
        }
    }

    // ========== 2. 워커 전체 처리량 (end-to-end) ==========

    @Test
    @Order(2)
    @DisplayName("워커 처리량 - 규모별 end-to-end")
    void 워커_처리량_측정() {
        int[] dataSizes = {1_000, 5_000, 10_000, 42_300};
        int batchSize = 500;

        System.out.println();
        System.out.println("=== 워커 처리량 측정 (배치 " + batchSize + "건) ===");
        System.out.printf("%-12s | %-15s | %-15s | %-12s%n", "데이터 규모", "총 처리 시간", "초당 처리량", "폴링 횟수");
        System.out.println("-".repeat(65));

        for (int size : dataSizes) {
            cleanUp();
            bulkInsertMessages(size);

            MessageWorkerProperties props = new MessageWorkerProperties(batchSize, 0, 5, 10);
            MessageDispatchWorker worker = new MessageDispatchWorker(
                    messageDispatchRepository, attemptRepository, adapterFactory, transactionTemplate, props);

            long start = System.nanoTime();
            int pollCount = 0;

            while (true) {
                long remaining = messageDispatchRepository.countByStatus(MessageStatus.PENDING);
                if (remaining == 0) break;
                worker.processMessages();
                pollCount++;
            }

            long elapsed = (System.nanoTime() - start) / 1_000_000;
            double throughput = size * 1000.0 / elapsed;

            System.out.printf("%-12s | %-15s | %-15s | %-12s%n",
                    String.format("%,d건", size),
                    elapsed + "ms",
                    String.format("%.0f건/초", throughput),
                    pollCount + "회");

            long accepted = messageDispatchRepository.countByStatus(MessageStatus.ACCEPTED);
            assertThat(accepted).isEqualTo(size);
        }
    }

    // ========== 3. 동시 워커 중복 선점 검증 ==========

    @Test
    @Order(3)
    @DisplayName("동시 워커 5개 - 중복 선점 없음 검증")
    void 동시_워커_중복_선점_없음() throws InterruptedException {
        int messageCount = 1_000;
        bulkInsertMessages(messageCount);

        int workerCount = 5;
        int batchSize = 100;
        MessageWorkerProperties props = new MessageWorkerProperties(batchSize, 0, 5, 10);

        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        CountDownLatch latch = new CountDownLatch(workerCount);
        ConcurrentLinkedQueue<Integer> claimedCounts = new ConcurrentLinkedQueue<>();

        long start = System.nanoTime();

        for (int i = 0; i < workerCount; i++) {
            executor.submit(() -> {
                try {
                    MessageDispatchWorker worker = new MessageDispatchWorker(
                            messageDispatchRepository, attemptRepository, adapterFactory, transactionTemplate, props);
                    int totalClaimed = 0;
                    // PENDING 또는 PROCESSING 상태 메시지가 남아있으면 계속 처리
                    while (true) {
                        long pending = messageDispatchRepository.countByStatus(MessageStatus.PENDING);
                        long processing = messageDispatchRepository.countByStatus(MessageStatus.PROCESSING);
                        if (pending == 0 && processing == 0) break;

                        long before = messageDispatchRepository.countByStatus(MessageStatus.ACCEPTED);
                        worker.processMessages();
                        long after = messageDispatchRepository.countByStatus(MessageStatus.ACCEPTED);
                        totalClaimed += (int) (after - before);
                    }
                    claimedCounts.add(totalClaimed);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(120, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long elapsed = (System.nanoTime() - start) / 1_000_000;
        int totalProcessed = claimedCounts.stream().mapToInt(Integer::intValue).sum();

        long accepted = messageDispatchRepository.countByStatus(MessageStatus.ACCEPTED);
        long pending = messageDispatchRepository.countByStatus(MessageStatus.PENDING);
        long processing = messageDispatchRepository.countByStatus(MessageStatus.PROCESSING);

        System.out.println();
        System.out.println("=== 동시 워커 중복 선점 검증 ===");
        System.out.printf("워커 수: %d, 메시지: %d건, 처리 시간: %dms%n", workerCount, messageCount, elapsed);
        System.out.printf("ACCEPTED: %d, PENDING: %d, PROCESSING: %d%n", accepted, pending, processing);

        // 핵심 검증: 중복 발송이 없으면 ACCEPTED == messageCount
        assertThat(accepted).isEqualTo(messageCount);
        assertThat(pending).isZero();
        assertThat(processing).isZero();
    }

    // ========== 4. 우선순위 보장 검증 ==========

    @Test
    @Order(4)
    @DisplayName("우선순위 - 출결 알림이 공지보다 먼저 선점")
    void 우선순위_보장_검증() {
        // 공지 500건 먼저 INSERT
        bulkInsertMessages(500, MessageType.ANNOUNCEMENT);
        // 출결 500건 나중에 INSERT
        bulkInsertMessages(500, MessageType.ATTENDANCE_CHECKIN);

        LocalDateTime claimTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String owner = "priority-test-worker";

        transactionTemplate.execute(status ->
                messageDispatchRepository.claimBatch(owner, claimTime, 5, 500));

        List<MessageDispatch> claimed = messageDispatchRepository.findClaimedMessages(owner, claimTime);

        // 선점된 500건이 전부 출결(priority=10)이어야 함
        long attendanceCount = claimed.stream()
                .filter(m -> m.getMessageType() == MessageType.ATTENDANCE_CHECKIN)
                .count();

        System.out.println();
        System.out.println("=== 우선순위 검증 ===");
        System.out.printf("선점 500건 중 출결 알림: %d건, 공지: %d건%n", attendanceCount, 500 - attendanceCount);

        assertThat(attendanceCount).isEqualTo(500);
    }

    // ========== Helper ==========

    private void bulkInsertMessages(int count) {
        bulkInsertMessages(count, MessageType.ATTENDANCE_CHECKIN);
    }

    private void bulkInsertMessages(int count, MessageType messageType) {
        int batchSize = 500;
        for (int i = 0; i < count; i += batchSize) {
            int end = Math.min(i + batchSize, count);
            List<MessageDispatch> batch = IntStream.range(i, end)
                    .mapToObj(idx -> MessageDispatch.createPending(
                            "T_BENCH00001",
                            "D_BENCH00001",
                            "G_" + String.format("%09d", idx),
                            "BENCHMARK",
                            "REF_" + idx,
                            messageType,
                            MessageChannel.SMS,
                            "벤치마크 메시지 제목",
                            "벤치마크 메시지 본문 #" + idx,
                            null))
                    .toList();
            messageDispatchRepository.saveAll(batch);
        }
        messageDispatchRepository.flush();
    }

    private long countByStatus(MessageStatus status) {
        return messageDispatchRepository.countByStatus(status);
    }
}
