package com.maru.controller.message.dto;

import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.RecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record BroadcastCreateReq(
        @NotBlank String title,
        @NotBlank @Size(max = 2000) String body,
        MessageChannel channel,
        @NotNull RecipientType recipientType,
        List<String> sectionIds,
        List<String> divisionIds,
        List<String> studentIds
) {
    public MessageChannel channelOrDefault() {
        return channel != null ? channel : MessageChannel.SMS;
    }
}
