package com.maru.controller.message.dto;

import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageStatus;
import com.maru.domain.message.MessageType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MessageSummaryRes(
        String id,
        MessageType messageType,
        MessageChannel channel,
        MessageStatus status,
        String title,
        LocalDateTime createdAt
) {
    public static MessageSummaryRes from(MessageDispatch message) {
        return new MessageSummaryRes(
                message.getId(),
                message.getMessageType(),
                message.getChannel(),
                message.getStatus(),
                message.getTitle(),
                message.getCreatedAt()
        );
    }
}
