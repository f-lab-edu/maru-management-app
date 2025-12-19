package com.maru.controller.payment;

import com.maru.controller.invoice.dto.PaymentStatisticsRes;
import com.maru.controller.invoice.dto.StudentPaymentHistoryRes;
import com.maru.controller.invoice.dto.UnpaidListRes;
import com.maru.security.CurrentUserId;
import com.maru.service.invoice.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

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
        List<UnpaidListRes> response = paymentService.getUnpaidList(dojangId);
        return ResponseEntity.ok(response);
    }

    /**
     * 수납 통계 조회 API
     *
     * @param dojangId 도장 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @param userId 현재 인증된 사용자 ID
     * @return 수납 통계
     */
    @GetMapping("/statistics")
    public ResponseEntity<PaymentStatisticsRes> getPaymentStatistics(
            @RequestParam Long dojangId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @CurrentUserId Long userId) {
        PaymentStatisticsRes response = paymentService.getPaymentStatistics(dojangId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 원생별 납부 내역 조회 API
     *
     * @param studentId 원생 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 납부 내역
     */
    @GetMapping("/students/{studentId}/history")
    public ResponseEntity<StudentPaymentHistoryRes> getStudentPaymentHistory(
            @PathVariable Long studentId,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        StudentPaymentHistoryRes response = paymentService.getStudentPaymentHistory(dojangId, studentId);
        return ResponseEntity.ok(response);
    }
}
