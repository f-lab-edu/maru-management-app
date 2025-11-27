package com.maru.service.sms;

import com.maru.common.exception.BusinessException;
import com.maru.config.properties.SmsVerificationProperties;
import com.maru.service.sms.dto.VerificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final SmsService smsService;
    private final VerificationCodeStore verificationCodeStore;
    private final SmsVerificationProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 인증번호 발송
     *
     * @param phone 전화번호
     * @return 만료 시간(초)
     */
    public int sendVerificationCode(String phone) {
        validateResendLimit(phone);
        String code = generateAndSaveCode(phone);
        sendSmsWithRollback(phone, code);
        return getExpirationSeconds();
    }

    /**
     * 인증번호 검증
     *
     * @param phone 전화번호
     * @param code 인증번호
     * @return 검증 결과 (성공 여부, 남은 시도 횟수)
     */
    public VerificationResult verifyCode(String phone, String code) {
        String storedCode = getStoredCodeOrThrow(phone);

        if (!storedCode.equals(code)) {
            return handleVerificationFailure(phone);
        }
        return handleVerificationSuccess(phone);
    }

    private void validateResendLimit(String phone) {
        if (verificationCodeStore.isResendLimited(phone)) {
            throw new BusinessException(SMS_RESEND_TOO_FAST);
        }
    }

    private String generateAndSaveCode(String phone) {
        String code = generateCode();
        Duration ttl = Duration.ofMinutes(properties.ttlMinutes());
        verificationCodeStore.save(phone, code, ttl);
        return code;
    }

    private void sendSmsWithRollback(String phone, String code) {
        String message = buildVerificationMessage(code);
        try {
            smsService.send(phone, message);
            log.info("인증번호 발송 완료: phone={}, provider={}", phone, smsService.getProviderName());
        } catch (BusinessException e) {
            verificationCodeStore.delete(phone);
            throw e;
        }
    }

    private String buildVerificationMessage(String code) {
        return String.format("[마루] 인증번호는 %s입니다.", code);
    }

    private int getExpirationSeconds() {
        return (int) Duration.ofMinutes(properties.ttlMinutes()).toSeconds();
    }

    private String getStoredCodeOrThrow(String phone) {
        VerificationCodeStatus status = verificationCodeStore.getStatus(phone);

        return switch (status) {
            case NOT_FOUND -> throw new BusinessException(SMS_CODE_NOT_FOUND);
            case EXPIRED -> {
                verificationCodeStore.delete(phone);
                throw new BusinessException(SMS_CODE_EXPIRED);
            }
            case VALID -> verificationCodeStore.get(phone).orElseThrow();
        };
    }

    private VerificationResult handleVerificationFailure(String phone) {
        int remainingAttempts = calculateRemainingAttempts(phone);

        if (remainingAttempts <= 0) {
            verificationCodeStore.delete(phone);
            log.warn("인증 시도 횟수 초과: phone={}", phone);
            throw new BusinessException(SMS_MAX_ATTEMPTS_EXCEEDED);
        }

        log.info("인증번호 불일치: phone={}, 남은 시도={}", phone, remainingAttempts);
        return VerificationResult.fail(remainingAttempts);
    }

    private int calculateRemainingAttempts(String phone) {
        int failCount = verificationCodeStore.incrementFailCount(phone);
        return properties.maxAttempts() - failCount;
    }

    private VerificationResult handleVerificationSuccess(String phone) {
        verificationCodeStore.delete(phone);
        log.info("인증번호 검증 완료: phone={}", phone);
        return VerificationResult.success();
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < properties.codeLength(); i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }
}
