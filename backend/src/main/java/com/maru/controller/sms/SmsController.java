package com.maru.controller.sms;

import com.maru.controller.sms.dto.SmsSendReq;
import com.maru.controller.sms.dto.SmsSendRes;
import com.maru.controller.sms.dto.SmsVerifyReq;
import com.maru.controller.sms.dto.SmsVerifyRes;
import com.maru.security.CurrentUserId;
import com.maru.service.sms.PhoneVerificationService;
import com.maru.service.user.UserMergeService;
import com.maru.service.user.dto.PhoneVerificationRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증")
@Slf4j
@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final PhoneVerificationService phoneVerificationService;
    private final UserMergeService userMergeService;

    /**
     * SMS 인증번호 발송 요청
     *
     * @param userId 요청자 ID
     * @param request 전화번호가 포함된 요청
     * @return 발송 결과
     */
    @PostMapping("/send")
    public ResponseEntity<SmsSendRes> sendVerificationCode(
            @CurrentUserId String userId,
            @Valid @RequestBody SmsSendReq request) {
        int expiresIn = phoneVerificationService.sendVerificationCode(request.phone(), userId);

        return ResponseEntity.ok(SmsSendRes.builder()
                .phone(request.phone())
                .expiresIn(expiresIn)
                .message("인증번호가 발송되었습니다")
                .build());
    }

    /**
     * SMS 인증번호 검증 및 계정 통합
     *
     * @param userId 요청자 ID
     * @param request 전화번호와 인증번호가 포함된 요청
     * @return 검증 결과 (계정 통합 여부 포함)
     */
    @PostMapping("/verify")
    public ResponseEntity<SmsVerifyRes> verifyCode(
            @CurrentUserId String userId,
            @Valid @RequestBody SmsVerifyReq request) {
        PhoneVerificationRes result = userMergeService.verifyPhoneAndMerge(
                userId, request.phone(), request.code());

        return ResponseEntity.ok(SmsVerifyRes.builder()
                .verified(true)
                .userId(result.userId())
                .isExistingUser(result.isExistingUser())
                .build());
    }
}
