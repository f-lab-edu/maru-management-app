package com.maru.repository.message;

import com.maru.domain.message.MessageQueue;
import com.maru.domain.message.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageQueueRepository extends JpaRepository<MessageQueue, Long> {

    /**
     * 재시도 대상 메시지 조회 (PENDING + scheduled_at 경과 + failed_count < maxRetry)
     *
     * @param status   조회할 상태 (PENDING)
     * @param time     현재 시간 (scheduledAt <= time)
     * @param maxRetry 최대 재시도 횟수
     * @param pageable 페이징 정보
     * @return 재시도 대상 메시지 목록
     */
    @Query("""
            SELECT m
            FROM MessageQueue m
            WHERE m.status = :status
              AND m.scheduledAt <= :time
              AND m.failedCount < :maxRetry
            ORDER BY m.scheduledAt ASC
            """)
    List<MessageQueue> findRetryTargets(
            @Param("status") MessageStatus status,
            @Param("time") LocalDateTime time,
            @Param("maxRetry") int maxRetry,
            Pageable pageable
    );

}
