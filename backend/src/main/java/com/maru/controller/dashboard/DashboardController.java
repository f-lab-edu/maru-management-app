package com.maru.controller.dashboard;

import com.maru.controller.dashboard.dto.DashboardNotificationRes;
import com.maru.controller.dashboard.dto.DashboardSummaryRes;
import com.maru.controller.dashboard.dto.RecentStudentRes;
import com.maru.domain.permission.PermissionType;
import com.maru.security.CurrentUserId;
import com.maru.security.RequirePermission;
import com.maru.service.dashboard.DashboardQueryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대시보드")
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    /**
     * 대시보드 요약 통계 조회
     *
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 대시보드 요약 통계
     */
    @GetMapping("/summary")
    @RequirePermission(PermissionType.STATS_VIEW_DASHBOARD)
    public ResponseEntity<DashboardSummaryRes> getSummary(
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        DashboardSummaryRes response = dashboardQueryService.getSummary(dojangId);
        return ResponseEntity.ok(response);
    }

    /**
     * 주요 알림 목록 조회
     * 최근 7일 이내 수납 완료, 사범 승인, 신규 입관 등
     *
     * @param dojangId 도장 ID
     * @param limit 조회할 개수 (기본 10)
     * @param offset 시작 위치 (기본 0)
     * @param userId 현재 사용자 ID
     * @return 알림 목록
     */
    @GetMapping("/notifications")
    public ResponseEntity<DashboardNotificationRes> getNotifications(
            @RequestParam String dojangId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @CurrentUserId String userId) {
        DashboardNotificationRes response = dashboardQueryService.getNotifications(dojangId, limit, offset);
        return ResponseEntity.ok(response);
    }

    /**
     * 최근 입관 원생 목록 조회
     * 입관일(enrolledAt)이 오늘 기준 7일 이내인 원생
     *
     * @param dojangId 도장 ID
     * @param limit 조회할 개수 (기본 10)
     * @param offset 시작 위치 (기본 0)
     * @param userId 현재 사용자 ID
     * @return 최근 입관 원생 목록
     */
    @GetMapping("/recent-students")
    public ResponseEntity<RecentStudentRes> getRecentStudents(
            @RequestParam String dojangId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @CurrentUserId String userId) {
        RecentStudentRes response = dashboardQueryService.getRecentStudents(dojangId, limit, offset);
        return ResponseEntity.ok(response);
    }
}
