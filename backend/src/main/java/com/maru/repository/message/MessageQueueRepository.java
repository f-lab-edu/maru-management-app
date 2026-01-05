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
import java.util.Optional;

public interface MessageQueueRepository extends JpaRepository<MessageQueue, String> {

    @Query("""
            SELECT m
            FROM MessageQueue m
            JOIN FETCH m.guardian
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

    @Query("""
            SELECT m
            FROM MessageQueue m
            JOIN FETCH m.guardian
            WHERE m.id = :id
            """)
    Optional<MessageQueue> findByIdWithGuardian(@Param("id") String id);

    @Modifying
    @Query("""
            UPDATE MessageQueue m
            SET m.status = :to
            WHERE m.id = :id
              AND m.status = :from
            """)
    int tryTransitionStatus(
            @Param("id") String id,
            @Param("from") MessageStatus from,
            @Param("to") MessageStatus to
    );

    @Modifying
    @Query("""
            UPDATE MessageQueue m
            SET m.status = 'PENDING'
            WHERE m.status = 'PROCESSING'
              AND m.updatedAt < :threshold
            """)
    int recoverStuckProcessing(@Param("threshold") LocalDateTime threshold);
}
