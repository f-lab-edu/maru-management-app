package com.maru.domain.message;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.message.exception.MessageBroadcastErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_broadcast")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageBroadcast extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, length = 13)
    private String tenantId;

    @Column(name = "dojang_id", nullable = false, length = 13)
    private String dojangId;

    @Column(name = "sent_by_user_id", nullable = false, length = 13)
    private String sentByUserId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 20)
    private RecipientType recipientType;

    @Column(name = "recipient_criteria", columnDefinition = "JSON")
    private String recipientCriteria;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MessageBroadcastStatus status;

    public static MessageBroadcast create(
            String tenantId,
            String dojangId,
            String sentByUserId,
            String title,
            String body,
            MessageChannel channel,
            RecipientType recipientType,
            String recipientCriteria,
            int totalCount,
            int skippedCount) {
        DomainAssert.hasText(title, MessageBroadcastErrorCode.EMPTY_TITLE);
        DomainAssert.hasText(body, MessageBroadcastErrorCode.EMPTY_BODY);

        MessageBroadcast broadcast = new MessageBroadcast();
        broadcast.tenantId = tenantId;
        broadcast.dojangId = dojangId;
        broadcast.sentByUserId = sentByUserId;
        broadcast.title = title;
        broadcast.body = body;
        broadcast.channel = channel;
        broadcast.recipientType = recipientType;
        broadcast.recipientCriteria = recipientCriteria;
        broadcast.totalCount = totalCount;
        broadcast.skippedCount = skippedCount;
        broadcast.status = MessageBroadcastStatus.CREATED;
        return broadcast;
    }

    /**
     * 발송 상태를 DISPATCHING으로 변경
     */
    public void startDispatching() {
        this.status = MessageBroadcastStatus.DISPATCHING;
    }
}
