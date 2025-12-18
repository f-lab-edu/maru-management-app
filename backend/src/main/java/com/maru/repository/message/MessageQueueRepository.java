package com.maru.repository.message;

import com.maru.domain.message.MessageQueue;
import com.maru.domain.message.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * 중복 발송 방지를 위한 선점 시도(조건부 UPDATE(반환값 1이면 선점 성공, 0이면 실패))
     *
     * @param id   메시지 ID
     * @param from 현재 상태 (예: PENDING)
     * @param to   변경할 상태 (예: PROCESSING)
     * @return 업데이트된 행 수 (1: 성공, 0: 실패)
     */
    @Modifying
    @Query("""
            UPDATE MessageQueue m
            SET m.status = :to
            WHERE m.id = :id
              AND m.status = :from
            """)
    int tryTransitionStatus(
            @Param("id") Long id,
            @Param("from") MessageStatus from,
            @Param("to") MessageStatus to
    );
}
