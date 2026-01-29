package com.maru.controller.message.dto;

import com.maru.domain.message.AttemptStatus;
import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.MessageDispatchAttempt;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AttemptRes(
        Long id,
        int attemptNumber,
        MessageChannel channel,
        AttemptStatus status,
        String vendorMessageId,
        String errorCode,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public static AttemptRes from(MessageDispatchAttempt attempt) {
        return new AttemptRes(
                attempt.getId(),
                attempt.getAttemptNumber(),
                attempt.getChannel(),
                attempt.getStatus(),
                attempt.getVendorMessageId(),
                attempt.getErrorCode(),
                attempt.getErrorMessage(),
                attempt.getStartedAt(),
                attempt.getCompletedAt()
        );
    }
}
