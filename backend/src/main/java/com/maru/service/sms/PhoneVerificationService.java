package com.maru.service.sms;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.sms.SmsErrorCode;
import com.maru.common.exception.sms.SmsVerificationException;
import com.maru.common.util.MaskingUtil;
import com.maru.config.properties.SmsVerificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

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
     * @param userId 요청자 ID
     * @return 만료 시간(초)
     */
    public int sendVerificationCode(String phone, String userId) {
        validateResendLimit(phone);
        String code = generateAndSaveCode(phone, userId);
        sendSmsWithRollback(phone, code);
        return getExpirationSeconds();
    }

    /**
     * 인증번호 검증
     *
     * @param phone 전화번호
     * @param code 인증번호
     * @param userId 검증 요청자 ID
     * @throws BusinessException SMS_CODE_NOT_FOUND, SMS_CODE_EXPIRED, SMS_MAX_ATTEMPTS_EXCEEDED, SMS_USER_MISMATCH
     * @throws SmsVerificationException SMS_CODE_INVALID (남은 시도 횟수 포함)
     */
    public void verifyCode(String phone, String code, String userId) {
        String storedCode = getStoredCodeOrThrow(phone);
        validateUserMatch(phone, userId);

        if (!storedCode.equals(code)) {
            handleVerificationFailure(phone);
            return;
        }
        handleVerificationSuccess(phone);
    }

    private void validateResendLimit(String phone) {
        if (verificationCodeStore.isResendLimited(phone)) {
            throw new BusinessException(SmsErrorCode.RESEND_TOO_FAST);
        }
    }

    private String generateAndSaveCode(String phone, String userId) {
        String code = generateCode();
        Duration ttl = Duration.ofMinutes(properties.ttlMinutes());
        verificationCodeStore.save(phone, code, userId, ttl);
        return code;
    }

    private void sendSmsWithRollback(String phone, String code) {
        String message = buildVerificationMessage(code);
        try {
            smsService.send(phone, message);
            log.info("인증번호 발송 완료: phone={}, provider={}", MaskingUtil.phone(phone), smsService.getProviderName());
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
            case NOT_FOUND -> throw new BusinessException(SmsErrorCode.CODE_NOT_FOUND);
            case EXPIRED -> {
                verificationCodeStore.delete(phone);
                throw new BusinessException(SmsErrorCode.CODE_EXPIRED);
            }
            case VALID -> verificationCodeStore.get(phone).orElseThrow();
        };
    }

    private void validateUserMatch(String phone, String userId) {
        String storedUserId = verificationCodeStore.getUserId(phone).orElse(null);
        if (storedUserId != null && !storedUserId.equals(userId)) {
            log.warn("인증 요청자 불일치: phone={}, storedUserId={}, requestUserId={}", MaskingUtil.phone(phone), storedUserId, userId);
            throw new BusinessException(SmsErrorCode.USER_MISMATCH);
        }
    }

    private void handleVerificationFailure(String phone) {
        int remainingAttempts = calculateRemainingAttempts(phone);

        if (remainingAttempts <= 0) {
            verificationCodeStore.delete(phone);
            log.warn("인증 시도 횟수 초과: phone={}", MaskingUtil.phone(phone));
            throw new BusinessException(SmsErrorCode.MAX_ATTEMPTS_EXCEEDED);
        }

        log.info("인증번호 불일치: phone={}, 남은 시도={}", MaskingUtil.phone(phone), remainingAttempts);
        throw new SmsVerificationException(SmsErrorCode.CODE_INVALID, remainingAttempts);
    }

    private int calculateRemainingAttempts(String phone) {
        int failCount = verificationCodeStore.incrementFailCount(phone);
        return properties.maxAttempts() - failCount;
    }

    private void handleVerificationSuccess(String phone) {
        verificationCodeStore.delete(phone);
        log.info("인증번호 검증 완료: phone={}", MaskingUtil.phone(phone));
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < properties.codeLength(); i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }
}
