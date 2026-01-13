package com.maru.controller.dashboard;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.CommonErrorCode;
import com.maru.controller.dashboard.dto.DashboardNotificationRes;
import com.maru.controller.dashboard.dto.DashboardSummaryRes;
import com.maru.controller.dashboard.dto.RecentStudentRes;
import com.maru.security.CurrentUserId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /**
     * 대시보드 요약 통계 조회
     *
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 대시보드 요약 통계
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryRes> getSummary(
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 주요 알림 목록 조회
     * 최근 7일 이내 수납 완료, 사범 승인, 신규 입관 등
     *
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 알림 목록
     */
    @GetMapping("/notifications")
    public ResponseEntity<DashboardNotificationRes> getNotifications(
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 최근 입관 원생 목록 조회
     * 입관일(enrolledAt)이 오늘 기준 7일 이내인 원생
     *
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 최근 입관 원생 목록
     */
    @GetMapping("/recent-students")
    public ResponseEntity<RecentStudentRes> getRecentStudents(
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
