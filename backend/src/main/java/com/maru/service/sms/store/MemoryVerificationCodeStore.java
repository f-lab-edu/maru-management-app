package com.maru.service.sms.store;

import com.maru.config.properties.SmsVerificationProperties;
import com.maru.service.sms.VerificationCodeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class MemoryVerificationCodeStore implements VerificationCodeStore {

    private final SmsVerificationProperties properties;
    private final ConcurrentHashMap<String, VerificationEntry> store = new ConcurrentHashMap<>();

    /**
     * 인증번호 저장
     *
     * @param phone 전화번호
     * @param code 인증번호
     * @param ttl 유효 기간
     */
    @Override
    public void save(String phone, String code, Duration ttl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);
        store.put(phone, new VerificationEntry(code, now, expiresAt));
    }

    /**
     * 인증번호 조회 (만료 시 자동 삭제)
     *
     * @param phone 전화번호
     * @return 인증번호 (만료되었거나 없으면 empty)
     */
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

        return Optional.of(entry.code);
    }

    /**
     * 인증번호 삭제
     *
     * @param phone 전화번호
     */
    @Override
    public void delete(String phone) {
        store.remove(phone);
    }

    /**
     * 재발송 제한 여부 확인 (만료 시 자동 삭제)
     *
     * @param phone 전화번호
     * @return 재발송 제한 중이면 true
     */
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

    /**
     * 인증 실패 횟수 증가
     *
     * @param phone 전화번호
     * @return 증가 후 실패 횟수 (만료되었거나 없으면 0)
     */
    @Override
    public int incrementFailCount(String phone) {
        VerificationEntry entry = store.get(phone);
        if (entry == null || entry.isExpired()) {
            return 0;
        }
        return entry.failCount.incrementAndGet();
    }

    /**
     * 인증 실패 횟수 조회
     *
     * @param phone 전화번호
     * @return 실패 횟수 (만료되었거나 없으면 0)
     */
    @Override
    public int getFailCount(String phone) {
        VerificationEntry entry = store.get(phone);
        if (entry == null || entry.isExpired()) {
            return 0;
        }
        return entry.failCount.get();
    }

    private static class VerificationEntry {
        private final String code;
        private final Instant createdAt;
        private final Instant expiresAt;
        private final AtomicInteger failCount;

        VerificationEntry(String code, Instant createdAt, Instant expiresAt) {
            this.code = code;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.failCount = new AtomicInteger(0);
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        boolean isWithinResendLimit(int resendLimitSeconds) {
            Instant resendAllowedAt = createdAt.plusSeconds(resendLimitSeconds);
            return Instant.now().isBefore(resendAllowedAt);
        }
    }
}
