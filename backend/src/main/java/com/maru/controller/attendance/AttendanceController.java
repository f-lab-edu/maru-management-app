package com.maru.controller.attendance;

import com.maru.controller.attendance.dto.*;
import com.maru.security.CurrentUserId;
import com.maru.service.attendance.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 단일 출석 체크 API
     *
     * @param dojangId 도장 ID
     * @param request 출석 체크 요청 정보
     * @param userId 현재 인증된 사용자 ID
     * @return 생성된 출석 기록
     */
    @PostMapping("/check")
    public ResponseEntity<AttendanceRes> checkIn(
            @RequestParam Long dojangId,
            @Valid @RequestBody AttendanceCheckReq request,
            @CurrentUserId Long userId) {
        AttendanceRes response = attendanceService.checkIn(
                dojangId,
                request.studentId(),
                request.method(),
                request.date(),
                request.checkinAt(),
                request.note());
        return ResponseEntity.ok(response);
    }

    /**
     * 일괄 출석 체크 API
     *
     * @param dojangId 도장 ID
     * @param request 일괄 출석 체크 요청 정보
     * @param userId 현재 인증된 사용자 ID
     * @return 성공/실패 결과
     */
    @PostMapping("/bulk-check")
    public ResponseEntity<BulkCheckRes> bulkCheckIn(
            @RequestParam Long dojangId,
            @Valid @RequestBody BulkCheckReq request,
            @CurrentUserId Long userId) {
        BulkCheckRes response = attendanceService.bulkCheckIn(
                dojangId,
                request.studentIds(),
                request.method());
        return ResponseEntity.ok(response);
    }

    /**
     * 퇴관 처리 API
     *
     * @param id 출석 기록 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 업데이트된 출석 기록
     */
    @PatchMapping("/{id}/checkout")
    public ResponseEntity<AttendanceRes> checkOut(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        AttendanceRes response = attendanceService.checkOut(dojangId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * 일괄 퇴관 처리 API
     *
     * @param dojangId 도장 ID
     * @param request 일괄 퇴관 요청 정보
     * @param userId 현재 인증된 사용자 ID
     * @return 성공/실패 결과
     */
    @PostMapping("/bulk-checkout")
    public ResponseEntity<BulkCheckRes> bulkCheckOut(
            @RequestParam Long dojangId,
            @Valid @RequestBody BulkCheckoutReq request,
            @CurrentUserId Long userId) {
        BulkCheckRes response = attendanceService.bulkCheckOut(dojangId, request.attendanceIds());
        return ResponseEntity.ok(response);
    }

    /**
     * 출석 상태 변경 API
     *
     * @param id 출석 기록 ID
     * @param dojangId 도장 ID
     * @param request 상태 변경 요청 정보
     * @param userId 현재 인증된 사용자 ID
     * @return 업데이트된 출석 기록
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AttendanceRes> changeStatus(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @Valid @RequestBody AttendanceStatusChangeReq request,
            @CurrentUserId Long userId) {
        AttendanceRes response = attendanceService.changeStatus(
                dojangId,
                id,
                request.status(),
                request.note());
        return ResponseEntity.ok(response);
    }

    /**
     * 오늘 출석 현황 조회 API
     *
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 오늘 출석 현황
     */
    @GetMapping("/today")
    public ResponseEntity<CurrentAttendanceRes> getTodayAttendance(
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        CurrentAttendanceRes response = attendanceService.getTodayAttendance(dojangId);
        return ResponseEntity.ok(response);
    }

    /**
     * 출석 이력 조회 API
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param userId 현재 인증된 사용자 ID
     * @return 출석 이력 목록
     */
    @GetMapping("/history")
    public ResponseEntity<List<AttendanceRes>> getHistory(
            @RequestParam Long dojangId,
            @RequestParam Long studentId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @CurrentUserId Long userId) {
        List<AttendanceRes> response = attendanceService.getHistory(dojangId, studentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 월간 출석 통계 조회 API
     *
     * @param dojangId 도장 ID
     * @param yearMonth 연월
     * @param userId 현재 인증된 사용자 ID
     * @return 월간 통계
     */
    @GetMapping("/stats/monthly")
    public ResponseEntity<AttendanceStatsRes> getMonthlyStats(
            @RequestParam Long dojangId,
            @RequestParam YearMonth yearMonth,
            @CurrentUserId Long userId) {
        AttendanceStatsRes response = attendanceService.getMonthlyStats(dojangId, yearMonth);
        return ResponseEntity.ok(response);
    }

    /**
     * 기간별 출석 현황 조회 API
     *
     * @param dojangId 도장 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param userId 현재 인증된 사용자 ID
     * @return 기간별 출석 현황
     */
    @GetMapping("/range")
    public ResponseEntity<RangeAttendanceRes> getAttendanceRange(
            @RequestParam Long dojangId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @CurrentUserId Long userId) {
        RangeAttendanceRes response = attendanceService.getAttendanceRange(dojangId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
