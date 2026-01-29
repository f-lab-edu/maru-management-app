package com.maru.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "message.worker")
public record MessageWorkerProperties(
        int batchSize,
        int pollDelayMs,
        int maxRetry,
        int stuckThresholdMinutes
) {
    public MessageWorkerProperties {
        if (batchSize <= 0) batchSize = 500;
        if (pollDelayMs <= 0) pollDelayMs = 3000;
        if (maxRetry <= 0) maxRetry = 5;
        if (stuckThresholdMinutes <= 0) stuckThresholdMinutes = 10;
    }
}
