package com.maru.controller.payment;

import com.maru.controller.invoice.dto.UnpaidListRes;
import com.maru.security.CurrentUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    /**
     * 미납자 목록 조회 API
     *
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 미납자 목록
     */
    @GetMapping("/unpaid")
    public ResponseEntity<List<UnpaidListRes>> getUnpaidList(
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
