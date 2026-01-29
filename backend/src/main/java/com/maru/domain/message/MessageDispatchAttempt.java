package com.maru.domain.message;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_dispatch_attempt")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDispatchAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id", nullable = false)
    private MessageDispatch dispatch;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MessageChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status;

    @Column(name = "processing_owner", length = 100)
    private String processingOwner;

    @Column(name = "vendor_message_id", length = 100)
    private String vendorMessageId;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 발송 시도 시작
     *
     * @param dispatchId MessageDispatchEntity
     * @param attemptNumber 시도 번호 (1부터 시작)
     * @param channel 발송 채널
     * @param owner 워커 인스턴스 ID
     * @return 생성된 MessageDispatchAttempt
     */
    public static MessageDispatchAttempt start(MessageDispatch dispatch, int attemptNumber,
                                               MessageChannel channel, String owner) {
        MessageDispatchAttempt attempt = new MessageDispatchAttempt();
        attempt.dispatch = dispatch;
        attempt.attemptNumber = attemptNumber;
        attempt.channel = channel;
        attempt.processingOwner = owner;
        attempt.startedAt = LocalDateTime.now();
        attempt.status = AttemptStatus.IN_PROGRESS;
        return attempt;
    }

    /**
     * 발송 시도 완료
     *
     * @param success 성공 여부
     * @param vendorMessageId 벤더가 부여한 메시지 ID (성공 시)
     * @param errorCode 에러 코드 (실패 시)
     * @param errorMessage 에러 메시지 (실패 시)
     */
    public void complete(boolean success, String vendorMessageId,
                         String errorCode, String errorMessage) {
        this.completedAt = LocalDateTime.now();
        this.status = success ? AttemptStatus.SUCCESS : AttemptStatus.FAILED;
        this.vendorMessageId = vendorMessageId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
