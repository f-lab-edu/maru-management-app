package com.maru.domain.message;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.message.exception.MessageQueueErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_queue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageQueue extends BaseEntity {

    @Column(nullable = false, length = 13)
    private String tenantId;

    @Column(nullable = false, length = 13)
    private String dojangId;

    @Column(name = "guardian_id", nullable = false, length = 13)
    private String guardianId;

    @Column(name = "student_id", length = 13)
    private String studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MessageType messageType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    @Column(columnDefinition = "TEXT")
    private String dataPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageStatus status = MessageStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @Column(nullable = false)
    private int failedCount = 0;

    @Column(length = 1000)
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

    /**
     * 출결 알림 메시지 생성
     *
     * @param tenantId   테넌트 ID
     * @param dojangId   도장 ID
     * @param guardianId 수신자 학부모 ID
     * @param studentId  수련생 ID
     * @param title      알림 제목
     * @param body       알림 본문
     * @return 생성된 MessageQueue
     */
    public static MessageQueue createAttendanceNotification(
            String tenantId,
            String dojangId,
            String guardianId,
            String studentId,
            String title,
            String body
    ) {
        DomainAssert.hasText(guardianId, MessageQueueErrorCode.GUARDIAN_REQUIRED);
        DomainAssert.hasText(title, MessageQueueErrorCode.TITLE_REQUIRED);
        DomainAssert.hasText(body, MessageQueueErrorCode.BODY_REQUIRED);

        MessageQueue message = new MessageQueue();
        message.tenantId = tenantId;
        message.dojangId = dojangId;
        message.guardianId = guardianId;
        message.studentId = studentId;
        message.messageType = MessageType.ATTENDANCE;
        message.title = title;
        message.body = body;
        message.status = MessageStatus.PENDING;
        message.scheduledAt = LocalDateTime.now();
        message.failedCount = 0;
        return message;
    }
}