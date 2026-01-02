package com.maru.controller.payment;

import com.maru.controller.invoice.dto.PaymentStatisticsRes;
import com.maru.controller.invoice.dto.StudentPaymentHistoryRes;
import com.maru.controller.invoice.dto.UnpaidListRes;
import com.maru.controller.invoice.dto.YearlyStatisticsRes;
import com.maru.security.CurrentUserId;
import com.maru.service.invoice.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        List<UnpaidListRes> response = paymentService.getUnpaidList(dojangId);
        return ResponseEntity.ok(response);
    }

    /**
     * 수납 통계 조회 API (청구 연월 기준)
     *
     * @param dojangId 도장 ID
     * @param year 청구 연도
     * @param month 청구 월 (1-12)
     * @param userId 현재 인증된 사용자 ID
     * @return 수납 통계
     */
    @GetMapping("/statistics")
    public ResponseEntity<PaymentStatisticsRes> getPaymentStatistics(
            @RequestParam String dojangId,
            @RequestParam int year,
            @RequestParam int month,
            @CurrentUserId String userId) {
        PaymentStatisticsRes response = paymentService.getPaymentStatistics(dojangId, year, month);
        return ResponseEntity.ok(response);
    }


    /**
     * 연간 수납 통계 조회 API (12개월 통계를 단일 쿼리로 조회)
     *
     * @param dojangId 도장 ID
     * @param year 조회 연도
     * @param userId 현재 인증된 사용자 ID
     * @return 연간 수납 통계 (월별 데이터 포함)
     */
    @GetMapping("/statistics/yearly")
    public ResponseEntity<YearlyStatisticsRes> getYearStatistics(
            @RequestParam String dojangId,
            @RequestParam int year,
            @CurrentUserId String userId) {
        YearlyStatisticsRes response = paymentService.getYearStatistics(dojangId, year);
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
            @PathVariable String studentId,
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        StudentPaymentHistoryRes response = paymentService.getStudentPaymentHistory(dojangId, studentId);
        return ResponseEntity.ok(response);
    }
}
