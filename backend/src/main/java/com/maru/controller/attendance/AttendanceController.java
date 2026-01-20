package com.maru.controller.attendance;

import com.maru.controller.attendance.dto.*;
import com.maru.security.CurrentUserId;
import com.maru.service.attendance.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "출결")
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 단일 출석 기록 생성 API
     *
     * @param dojangId 도장 ID
     * @param request 출석 체크 요청 정보
     * @param userId 현재 인증된 사용자 ID
     * @return 생성된 출석 기록
     */
    @PostMapping("/check")
    public ResponseEntity<AttendanceRes> checkIn(
            @RequestParam String dojangId,
            @Valid @RequestBody AttendanceCheckReq request,
            @CurrentUserId String userId) {
        AttendanceRes response = attendanceService.checkIn(
                dojangId,
                request.studentId(),
                request.method(),
                request.status(),
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
            @RequestParam String dojangId,
            @Valid @RequestBody BulkCheckReq request,
            @CurrentUserId String userId) {
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
            @PathVariable String id,
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        AttendanceRes response = attendanceService.checkOut(dojangId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * 퇴관 취소 API
     *
     * @param id 출석 기록 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 업데이트된 출석 기록
     */
    @PatchMapping("/{id}/cancel-checkout")
    public ResponseEntity<AttendanceRes> cancelCheckout(
            @PathVariable String id,
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        AttendanceRes response = attendanceService.cancelCheckout(dojangId, id);
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
            @RequestParam String dojangId,
            @Valid @RequestBody BulkCheckoutReq request,
            @CurrentUserId String userId) {
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
            @PathVariable String id,
            @RequestParam String dojangId,
            @Valid @RequestBody AttendanceStatusChangeReq request,
            @CurrentUserId String userId) {
        AttendanceRes response = attendanceService.changeStatus(
                dojangId,
                id,
                request.status(),
                request.note());
        return ResponseEntity.ok(response);
    }

    /**
     * 출석 시간 변경 API
     *
     * @param id 출석 기록 ID
     * @param dojangId 도장 ID
     * @param request 시간 변경 요청 정보
     * @param userId 현재 인증된 사용자 ID
     * @return 업데이트된 출석 기록
     */
    @PatchMapping("/{id}/time")
    public ResponseEntity<AttendanceRes> changeTime(
            @PathVariable String id,
            @RequestParam String dojangId,
            @Valid @RequestBody AttendanceTimeChangeReq request,
            @CurrentUserId String userId) {
        AttendanceRes response = attendanceService.changeTime(
                dojangId,
                id,
                request.checkinAt(),
                request.checkoutAt());
        return ResponseEntity.ok(response);
    }

    /**
     * 일괄 출석 상태 변경 API
     *
     * @param dojangId 도장 ID
     * @param request 일괄 상태 변경 요청 정보
     * @param userId 현재 인증된 사용자 ID
     * @return 성공/실패 결과
     */
    @PostMapping("/bulk-status")
    public ResponseEntity<BulkCheckRes> bulkChangeStatus(
            @RequestParam String dojangId,
            @Valid @RequestBody BulkStatusChangeReq request,
            @CurrentUserId String userId) {
        BulkCheckRes response = attendanceService.bulkChangeStatus(
                dojangId,
                request.attendanceIds(),
                request.status(),
                request.note());
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
            @RequestParam String dojangId,
            @RequestParam String studentId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @CurrentUserId String userId) {
        List<AttendanceRes> response = attendanceService.getHistory(dojangId, studentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 기간별 출석 현황 조회 API
     *
     * @param dojangId 도장 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param sectionId 수련부 ID (선택)
     * @param divisionId 수련반 ID (선택)
     * @param userId 현재 인증된 사용자 ID
     * @return 기간별 출석 현황
     */
    @GetMapping("/range")
    public ResponseEntity<RangeAttendanceRes> getAttendanceRange(
            @RequestParam String dojangId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String sectionId,
            @RequestParam(required = false) String divisionId,
            @CurrentUserId String userId) {
        RangeAttendanceRes response = attendanceService.getAttendanceRange(dojangId, startDate, endDate, sectionId, divisionId);
        return ResponseEntity.ok(response);
    }
}
