package com.maru.service.sms.store;

import com.maru.config.properties.SmsVerificationProperties;
import com.maru.service.sms.VerificationCodeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MemoryVerificationCodeStore implements VerificationCodeStore {

    private final SmsVerificationProperties properties;
    private final ConcurrentHashMap<String, VerificationEntry> store = new ConcurrentHashMap<>();

    @Override
    public void save(String phone, String code, Duration ttl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);
        store.put(phone, new VerificationEntry(code, now, expiresAt));
    }

    @Override
    public Optional<String> get(String phone) {
        VerificationEntry entry = store.get(phone);
        if (entry == null) {
            return Optional.empty();
        }

        if (entry.isExpired()) {
            store.remove(phone);
            return Optional.empty();
        }

        return Optional.of(entry.code());
    }

    @Override
    public void delete(String phone) {
        store.remove(phone);
    }

    @Override
    public boolean exists(String phone) {
        VerificationEntry entry = store.get(phone);
        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            store.remove(phone);
            return false;
        }

        return entry.isWithinResendLimit(properties.resendLimitSeconds());
    }

    private record VerificationEntry(String code, Instant createdAt, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        boolean isWithinResendLimit(int resendLimitSeconds) {
            Instant resendAllowedAt = createdAt.plusSeconds(resendLimitSeconds);
            return Instant.now().isBefore(resendAllowedAt);
        }
    }
}
