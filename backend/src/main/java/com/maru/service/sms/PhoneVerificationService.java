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
        if (verificationCodeStore.exists(phone)) {
            throw new BusinessException(SMS_RESEND_TOO_FAST);
        }

        String code = generateCode();
        Duration ttl = Duration.ofMinutes(properties.ttlMinutes());
        verificationCodeStore.save(phone, code, ttl);

        String message = String.format("[마루] 인증번호는 %s입니다.", code);
        smsService.send(phone, message);

        log.info("인증번호 발송 완료: phone={}, provider={}", phone, smsService.getProviderName());
        return (int) ttl.toSeconds();
    }

    /**
     * 인증번호 검증
     *
     * @param phone 전화번호
     * @param code 인증번호
     * @return 검증 결과 (성공 여부, 남은 시도 횟수)
     */
    public VerificationResult verifyCode(String phone, String code) {
        String storedCode = verificationCodeStore.get(phone)
                .orElseThrow(() -> new BusinessException(SMS_CODE_NOT_FOUND));

        if (!storedCode.equals(code)) {
            int failCount = verificationCodeStore.incrementFailCount(phone);
            int remainingAttempts = properties.maxAttempts() - failCount;

            if (remainingAttempts <= 0) {
                verificationCodeStore.delete(phone);
                log.warn("인증 시도 횟수 초과: phone={}", phone);
                throw new BusinessException(SMS_CODE_EXPIRED);
            }

            log.info("인증번호 불일치: phone={}, 남은 시도={}", phone, remainingAttempts);
            return VerificationResult.fail(remainingAttempts);
        }

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
