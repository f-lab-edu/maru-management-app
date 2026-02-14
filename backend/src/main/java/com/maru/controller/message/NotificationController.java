package com.maru.controller.message;

import com.maru.controller.message.dto.NotificationDailySummaryRes;
import com.maru.controller.message.dto.NotificationDetailRes;
import com.maru.security.CurrentUserId;
import com.maru.service.message.MessageQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "자동 알림 조회")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final MessageQueryService queryService;

    /**
     * 자동 발송 일별 요약 조회
     *
     * @param dojangId 도장 ID
     * @param userId 사용자 ID
     * @param pageable 페이징
     * @return 일별 발송 요약 목록
     */
    @GetMapping("/summary")
    public ResponseEntity<Page<NotificationDailySummaryRes>> getSummary(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(queryService.findNotificationSummary(dojangId, pageable));
    }

    /**
     * 자동 발송 상세 목록 조회
     *
     * @param dojangId 도장 ID
     * @param userId 사용자 ID
     * @param date 발송 날짜
     * @param messageType 메시지 타입
     * @param pageable 페이징
     * @return 발송 상세 목록
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDetailRes>> getDetails(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @RequestParam LocalDate date,
            @RequestParam String messageType,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(queryService.findNotificationDetails(dojangId, date, messageType, pageable));
    }
}
