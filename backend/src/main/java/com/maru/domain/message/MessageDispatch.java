package com.maru.domain.message;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.message.exception.MessageDispatchErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_dispatch")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDispatch extends BaseEntity {

    private static final int[] BACKOFF_SECONDS = {30, 120, 600, 1800, 3600};

    @Column(nullable = false, length = 13)
    private String tenantId;

    @Column(nullable = false, length = 13)
    private String dojangId;

    @Column(name = "guardian_id", nullable = false, length = 13)
    private String guardianId;

    @Column(name = "ref_type", length = 30)
    private String refType;

    @Column(name = "ref_id", length = 13)
    private String refId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MessageType messageType;

    @Column(nullable = false)
    private int priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MessageChannel channel = MessageChannel.SMS;

    @Column(length = 200)
    private String title;

    @Column(length = 500)
    private String body;

    @Column(columnDefinition = "TEXT")
    private String dataPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageStatus status = MessageStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "processing_owner", length = 100)
    private String processingOwner;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "vendor_message_id", length = 100)
    private String vendorMessageId;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(nullable = false)
    private int failedCount = 0;

    @Column(length = 1000)
    private String errorMessage;

    /**
     * Fat Outbox용 팩토리 메서드 - 메시지 본문을 생성 시점에 저장
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param guardianId 보호자 ID
     * @param refType 참조 도메인 타입 (ATTENDANCE, PAYMENT 등)
     * @param refId 참조 ID
     * @param messageType 메시지 유형
     * @param channel 발송 채널
     * @param title 메시지 제목
     * @param body 메시지 본문
     * @param dataPayload 추가 데이터 (JSON)
     * @return 생성된 MessageDispatch (PENDING 상태)
     */
    public static MessageDispatch createPending(
            String tenantId,
            String dojangId,
            String guardianId,
            String refType,
            String refId,
            MessageType messageType,
            MessageChannel channel,
            String title,
            String body,
            String dataPayload) {
        DomainAssert.hasText(guardianId, MessageDispatchErrorCode.GUARDIAN_REQUIRED);
        DomainAssert.hasText(title, MessageDispatchErrorCode.TITLE_REQUIRED);
        DomainAssert.hasText(body, MessageDispatchErrorCode.BODY_REQUIRED);

        MessageDispatch md = new MessageDispatch();
        md.tenantId = tenantId;
        md.dojangId = dojangId;
        md.guardianId = guardianId;
        md.refType = refType;
        md.refId = refId;
        md.messageType = messageType;
        md.priority = messageType.getPriority();
        md.channel = channel;
        md.title = title;
        md.body = body;
        md.dataPayload = dataPayload;
        md.status = MessageStatus.PENDING;
        md.scheduledAt = LocalDateTime.now();
        md.nextRetryAt = LocalDateTime.now();
        md.failedCount = 0;
        return md;
    }

    public void markAsProcessing() {
        this.status = MessageStatus.PROCESSING;
    }

    /**
     * 처리 시작 마킹 (워커에서 호출)
     *
     * @param owner 워커 인스턴스 ID
     * @param startedAt 처리 시작 시각
     */
    public void markAsProcessing(String owner, LocalDateTime startedAt) {
        this.status = MessageStatus.PROCESSING;
        this.processingOwner = owner;
        this.processingStartedAt = startedAt;
    }

    public void markAsSent() {
        this.status = MessageStatus.ACCEPTED;
        this.sentAt = LocalDateTime.now();
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * 발송 성공 (최종 상태)
     *
     * @param vendorMessageId 벤더가 부여한 메시지 ID
     */
    public void markAsAccepted(String vendorMessageId) {
        this.status = MessageStatus.ACCEPTED;
        this.vendorMessageId = vendorMessageId;
        this.sentAt = LocalDateTime.now();
        this.acceptedAt = LocalDateTime.now();
        this.processingOwner = null;
        this.processingStartedAt = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = MessageStatus.DEAD;
        this.errorMessage = errorMessage;
    }

    /**
     * 최종 실패 (렌더링 실패 또는 재시도 초과)
     *
     * @param errorMessage 에러 메시지
     * @throws IllegalStateException ACCEPTED 상태에서 호출 시
     */
    public void markAsDead(String errorMessage) {
        if (this.status == MessageStatus.ACCEPTED) {
            throw new IllegalStateException("ACCEPTED 상태에서 DEAD 전환 불가: " + getId());
        }

        this.status = MessageStatus.DEAD;
        this.errorMessage = errorMessage;
        this.processingOwner = null;
        this.processingStartedAt = null;
    }

    public void markAsPending() {
        this.status = MessageStatus.PENDING;
    }

    /**
     * DEAD 상태 메시지를 재발송 가능 상태로 초기화
     *
     * @throws IllegalStateException DEAD 상태가 아닌 경우
     */
    public void resetForResend() {
        if (this.status != MessageStatus.DEAD) {
            throw new IllegalStateException("DEAD 상태에서만 재발송 가능: " + getId());
        }

        this.status = MessageStatus.PENDING;
        this.failedCount = 0;
        this.nextRetryAt = null;
        this.errorMessage = null;
        this.processingOwner = null;
        this.processingStartedAt = null;
    }

    /**
     * 발송 실패 시 재시도 스케줄링 (backoff 적용)
     *
     * @param errorMessage 에러 메시지
     * @throws IllegalStateException ACCEPTED/DEAD 상태에서 호출 시
     */
    public void scheduleRetry(String errorMessage) {
        if (this.status == MessageStatus.ACCEPTED || this.status == MessageStatus.DEAD) {
            throw new IllegalStateException("ACCEPTED/DEAD 상태에서 재시도 불가: " + getId());
        }

        this.status = MessageStatus.PENDING;
        this.errorMessage = errorMessage;
        this.processingOwner = null;
        this.processingStartedAt = null;

        int idx = Math.min(this.failedCount, BACKOFF_SECONDS.length - 1);
        int base = BACKOFF_SECONDS[idx];
        int jitter = (int) (base * 0.2 * (Math.random() * 2 - 1));
        this.nextRetryAt = LocalDateTime.now().plusSeconds(base + jitter);
    }

    public void incrementFailedCount() {
        this.failedCount++;
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @param maxRetry 최대 재시도 횟수
     * @return 재시도 가능 여부
     */
    public boolean canRetry(int maxRetry) {
        return this.failedCount < maxRetry
                && this.status != MessageStatus.ACCEPTED
                && this.status != MessageStatus.DEAD;
    }
}
