package com.maru.service.notification.result;

public record SendResult(
        String messageId,
        boolean success,
        String vendorMessageId,
        String errorCode,
        String errorMessage
) {
    public static SendResult success(String messageId, String vendorMessageId) {
        return new SendResult(messageId, true, vendorMessageId, null, null);
    }

    public static SendResult failure(String messageId, String errorCode, String errorMessage) {
        return new SendResult(messageId, false, null, errorCode, errorMessage);
    }
}
