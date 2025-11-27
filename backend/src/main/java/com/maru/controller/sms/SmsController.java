package com.maru.controller.sms;

import com.maru.controller.sms.dto.SmsSendReq;
import com.maru.controller.sms.dto.SmsSendRes;
import com.maru.controller.sms.dto.SmsVerifyReq;
import com.maru.controller.sms.dto.SmsVerifyRes;
import com.maru.service.sms.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final PhoneVerificationService phoneVerificationService;

    /**
     * SMS 인증번호 발송 요청
     *
     * @param request 전화번호가 포함된 요청
     * @return 발송 결과
     */
    @PostMapping("/send")
    public ResponseEntity<SmsSendRes> sendVerificationCode(
            @Valid @RequestBody SmsSendReq request) {
        int expiresIn = phoneVerificationService.sendVerificationCode(request.phone());

        return ResponseEntity.ok(SmsSendRes.builder()
                .phone(request.phone())
                .expiresIn(expiresIn)
                .message("인증번호가 발송되었습니다")
                .build());
    }

    /**
     * SMS 인증번호 검증
     *
     * @param request 전화번호와 인증번호가 포함된 요청
     * @return 검증 결과
     */
    @PostMapping("/verify")
    public ResponseEntity<SmsVerifyRes> verifyCode(
            @Valid @RequestBody SmsVerifyReq request) {
        phoneVerificationService.verifyCode(request.phone(), request.code());

        return ResponseEntity.ok(SmsVerifyRes.builder()
                .verified(true)
                .userId(null)
                .isExistingUser(false)
                .build());
    }
}
