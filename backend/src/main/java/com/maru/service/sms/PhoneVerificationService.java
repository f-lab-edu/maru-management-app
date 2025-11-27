package com.maru.service.sms;

import com.maru.common.exception.BusinessException;
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

    private static final int CODE_LENGTH = 6;
    private static final Duration CODE_TTL = Duration.ofMinutes(3);

    private final SmsService smsService;
    private final VerificationCodeStore verificationCodeStore;
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
        verificationCodeStore.save(phone, code, CODE_TTL);

        String message = String.format("[마루] 인증번호는 %s입니다.", code);
        smsService.send(phone, message);

        log.info("인증번호 발송 완료: phone={}, provider={}", phone, smsService.getProviderName());
        return (int) CODE_TTL.toSeconds();
    }

    /**
     * 인증번호 검증
     *
     * @param phone 전화번호
     * @param code 인증번호
     * @return 검증 성공 여부
     */
    public boolean verifyCode(String phone, String code) {
        String storedCode = verificationCodeStore.get(phone)
                .orElseThrow(() -> new BusinessException(SMS_CODE_NOT_FOUND));

        if (!storedCode.equals(code)) {
            throw new BusinessException(SMS_CODE_INVALID);
        }

        verificationCodeStore.delete(phone);
        log.info("인증번호 검증 완료: phone={}", phone);
        return true;
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }
}
