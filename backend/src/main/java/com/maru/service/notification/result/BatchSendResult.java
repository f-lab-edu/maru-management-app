package com.maru.service.notification.result;

import java.util.List;
import java.util.Map;

public record BatchSendResult(Map<String, SendResult> results) {

    public static BatchSendResult empty() {
        return new BatchSendResult(Map.of());
    }

    public List<String> getSuccessIds() {
        return results.entrySet().stream()
                .filter(e -> e.getValue().success())
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getFailureIds() {
        return results.entrySet().stream()
                .filter(e -> !e.getValue().success())
                .map(Map.Entry::getKey)
                .toList();
    }
}
