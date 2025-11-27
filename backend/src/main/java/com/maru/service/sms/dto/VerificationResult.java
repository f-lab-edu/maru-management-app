package com.maru.service.sms.dto;

import lombok.Builder;

@Builder
public record VerificationResult(
        boolean verified,
        int remainingAttempts
) {
    public static VerificationResult success() {
        return new VerificationResult(true, 0);
    }

    public static VerificationResult fail(int remainingAttempts) {
        return new VerificationResult(false, remainingAttempts);
    }
}
