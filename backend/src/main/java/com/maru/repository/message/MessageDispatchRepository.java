package com.maru.repository.message;

import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageStatus;
import com.maru.domain.message.MessageType;
import com.maru.repository.message.view.BroadcastStatusCountView;
import com.maru.repository.message.view.MonthlySummaryView;
import com.maru.repository.message.view.StatusCountView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
            ORDER BY priority ASC, next_retry_at ASC, id ASC
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

    @Query("""
            SELECT m.status AS status, COUNT(m) AS count
            FROM MessageDispatch m
            WHERE m.refType = 'BROADCAST' AND m.refId = :broadcastId
            GROUP BY m.status
            """)
    List<StatusCountView> countByBroadcastIdGroupByStatus(@Param("broadcastId") String broadcastId);

    @Query("""
            SELECT m.refId AS broadcastId, m.status AS status, COUNT(m) AS count
            FROM MessageDispatch m
            WHERE m.refType = 'BROADCAST' AND m.refId IN :broadcastIds
            GROUP BY m.refId, m.status
            """)
    List<BroadcastStatusCountView> countByBroadcastIdsGroupByStatus(
            @Param("broadcastIds") List<String> broadcastIds);

    @Query("""
            SELECT m
            FROM MessageDispatch m
            WHERE m.refType = 'BROADCAST' AND m.refId = :broadcastId
            ORDER BY m.createdAt DESC
            """)
    Page<MessageDispatch> findByBroadcastId(@Param("broadcastId") String broadcastId, Pageable pageable);

    @Query("""
            SELECT m
            FROM MessageDispatch m
            WHERE m.refType = 'BROADCAST' AND m.refId = :broadcastId
              AND m.status = :status
            ORDER BY m.createdAt DESC
            """)
    Page<MessageDispatch> findByBroadcastIdAndStatus(
            @Param("broadcastId") String broadcastId,
            @Param("status") MessageStatus status,
            Pageable pageable);

    @Query("""
            SELECT m
            FROM MessageDispatch m
            WHERE m.refType = 'BROADCAST' AND m.refId = :broadcastId
              AND m.status = :status
            """)
    List<MessageDispatch> findAllByBroadcastIdAndStatus(
            @Param("broadcastId") String broadcastId,
            @Param("status") MessageStatus status);

    @Query("""
            SELECT m
            FROM MessageDispatch m
            WHERE m.dojangId = :dojangId
              AND m.messageType = :messageType
              AND m.createdAt >= :startOfDay
              AND m.createdAt < :startOfNextDay
            ORDER BY m.createdAt DESC
            """)
    Page<MessageDispatch> findNotificationDetails(
            @Param("dojangId") String dojangId,
            @Param("messageType") MessageType messageType,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            Pageable pageable);

    @Query("""
            SELECT m
            FROM MessageDispatch m
            WHERE m.dojangId = :dojangId
              AND m.messageType <> com.maru.domain.message.MessageType.ANNOUNCEMENT
              AND m.createdAt >= :startOfDay
              AND m.createdAt < :startOfNextDay
            ORDER BY m.createdAt DESC
            """)
    Page<MessageDispatch> findNotificationsByDate(
            @Param("dojangId") String dojangId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            Pageable pageable);

    @Query(value = """
            SELECT
                CASE WHEN md.message_type = 'ANNOUNCEMENT' THEN 'BROADCAST' ELSE 'AUTO' END AS category,
                COUNT(*) AS totalCount
            FROM message_dispatch md
            WHERE md.dojang_id = :dojangId
              AND md.created_at >= :startOfMonth
              AND md.created_at < :startOfNextMonth
            GROUP BY category
            """, nativeQuery = true)
    List<MonthlySummaryView> findMonthlyUsage(
            @Param("dojangId") String dojangId,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("startOfNextMonth") LocalDateTime startOfNextMonth);
}
