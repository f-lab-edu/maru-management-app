package com.maru.controller.message;

import com.maru.controller.message.dto.MonthlyUsageRes;
import com.maru.controller.message.dto.NotificationDetailRes;
import com.maru.security.CurrentUserId;
import com.maru.service.message.MessageQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@Tag(name = "자동 알림 조회")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final MessageQueryService queryService;

    /**
     * 자동 발송 타임라인 조회 (날짜 네비게이션 + 유형 필터)
     *
     * @param dojangId 도장 ID
     * @param userId 사용자 ID
     * @param date 조회 날짜 (null이면 오늘)
     * @param messageType 메시지 유형 필터 (null이면 전체)
     * @param pageable 페이징
     * @return 발송 타임라인 목록
     */
    @GetMapping("/timeline")
    public ResponseEntity<Page<NotificationDetailRes>> getTimeline(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String messageType,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(queryService.findNotificationTimeline(dojangId, date, messageType, pageable));
    }

    /**
     * 월별 메시지 사용량 조회 (자동알림 + 단체문자 합산)
     *
     * @param dojangId 도장 ID
     * @param userId 사용자 ID
     * @param yearMonth 조회 월 (yyyy-MM, null이면 현재 월)
     * @return 월별 사용량 통계
     */
    @GetMapping("/monthly-usage")
    public ResponseEntity<MonthlyUsageRes> getMonthlyUsage(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return ResponseEntity.ok(queryService.findMonthlyUsage(dojangId, yearMonth));
    }
}
