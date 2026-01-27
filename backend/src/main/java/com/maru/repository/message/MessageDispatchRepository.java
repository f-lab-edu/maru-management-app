package com.maru.repository.message;

import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageDispatchRepository extends JpaRepository<MessageDispatch, String> {

    @Query("""
            SELECT m
            FROM MessageDispatch m
            WHERE m.status = :status
              AND m.scheduledAt <= :time
              AND m.failedCount < :maxRetry
            ORDER BY m.scheduledAt ASC
            """)
    List<MessageDispatch> findRetryTargets(
            @Param("status") MessageStatus status,
            @Param("time") LocalDateTime time,
            @Param("maxRetry") int maxRetry,
            Pageable pageable
    );

    @Modifying
    @Query("""
            UPDATE MessageDispatch m
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
            UPDATE MessageDispatch m
            SET m.status = 'PENDING',
                m.processingOwner = NULL,
                m.processingStartedAt = NULL
            WHERE m.status = 'PROCESSING'
              AND m.processingStartedAt < :threshold
            """)
    int recoverStuckProcessing(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query(value = """
            UPDATE message_dispatch
            SET status = 'PROCESSING',
                processing_owner = :owner,
                processing_started_at = :claimTime
            WHERE status = 'PENDING'
              AND next_retry_at <= :claimTime
              AND failed_count < :maxRetry
            ORDER BY next_retry_at ASC
            LIMIT :batchSize
            """, nativeQuery = true)
    int claimBatch(
            @Param("owner") String owner,
            @Param("claimTime") LocalDateTime claimTime,
            @Param("maxRetry") int maxRetry,
            @Param("batchSize") int batchSize
    );

    @Query("""
            SELECT m FROM MessageDispatch m
            WHERE m.processingOwner = :owner
              AND m.status = 'PROCESSING'
              AND m.processingStartedAt = :claimTime
            """)
    List<MessageDispatch> findClaimedMessages(
            @Param("owner") String owner,
            @Param("claimTime") LocalDateTime claimTime
    );

    List<MessageDispatch> findByRefTypeAndRefId(String refType, String refId);

    @Query("""
            SELECT m FROM MessageDispatch m
            WHERE m.guardianId = :guardianId
            ORDER BY m.createdAt DESC
            """)
    Page<MessageDispatch> findByGuardianId(@Param("guardianId") String guardianId, Pageable pageable);

    @Query("""
            SELECT m FROM MessageDispatch m
            WHERE m.guardianId = :guardianId
              AND m.status = :status
            ORDER BY m.createdAt DESC
            """)
    Page<MessageDispatch> findByGuardianIdAndStatus(
            @Param("guardianId") String guardianId,
            @Param("status") MessageStatus status,
            Pageable pageable);
}
