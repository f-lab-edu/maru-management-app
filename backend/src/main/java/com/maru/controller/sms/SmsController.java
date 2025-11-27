package com.maru.controller.sms;

import com.maru.controller.sms.dto.SmsSendReq;
import com.maru.controller.sms.dto.SmsVerifyReq;
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

    /**
     * SMS 인증번호 발송 요청
     *
     * @param request 전화번호가 포함된 요청
     * @return 발송 결과
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationCode(
        @Valid @RequestBody SmsSendReq request) {
        throw new UnsupportedOperationException("구현 예정");
    }

    /**
     * SMS 인증번호 검증
     *
     * @param request 전화번호와 인증번호가 포함된 요청
     * @return 검증 결과
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(
        @Valid @RequestBody SmsVerifyReq request) {
        throw new UnsupportedOperationException("구현 예정");
    }
}
