package com.maru.controller.message.dto;

import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageStatus;
import com.maru.domain.message.MessageType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MessageDetailRes(
        String id,
        MessageType messageType,
        MessageChannel channel,
        MessageStatus status,
        String title,
        String body,
        int failedCount,
        String errorMessage,
        String vendorMessageId,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt
) {
    public static MessageDetailRes from(MessageDispatch message) {
        return new MessageDetailRes(
                message.getId(),
                message.getMessageType(),
                message.getChannel(),
                message.getStatus(),
                message.getTitle(),
                message.getBody(),
                message.getFailedCount(),
                message.getErrorMessage(),
                message.getVendorMessageId(),
                message.getCreatedAt(),
                message.getAcceptedAt()
        );
    }
}
