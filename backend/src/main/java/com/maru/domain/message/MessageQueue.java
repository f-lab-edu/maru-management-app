package com.maru.domain.message;

import com.maru.domain.common.BaseEntity;
import com.maru.domain.guardian.Guardian;
import com.maru.domain.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "message_queue",
    indexes = {
        @Index(name = "idx_message_queue_status_scheduled_at", columnList = "status, scheduled_at"),
        @Index(name = "idx_message_queue_tenant_id_created_at", columnList = "tenant_id, created_at"),
        @Index(name = "idx_message_queue_guardian_id", columnList = "guardian_id"),
        @Index(name = "idx_message_queue_student_id", columnList = "student_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageQueue extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "dojang_id", nullable = false)
    private Long dojangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    private MessageType messageType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Column(name = "data_payload", columnDefinition = "TEXT")
    private String dataPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status = MessageStatus.PENDING;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failed_count", nullable = false)
    private int failedCount = 0;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public void markAsProcessing() {
        this.status = MessageStatus.PROCESSING;
    }

    public void markAsSent() {
        this.status = MessageStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = MessageStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markAsPending() {
        this.status = MessageStatus.PENDING;
    }

    public void incrementFailedCount() {
        this.failedCount++;
    }

    public String getRecipientPhone() {
        return guardian.getPhone();
    }
}