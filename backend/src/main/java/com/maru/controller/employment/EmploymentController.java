package com.maru.controller.employment;

import com.maru.controller.employment.dto.EmploymentRes;
import com.maru.controller.employment.dto.InstructorDetailRes;
import com.maru.controller.employment.dto.InstructorRes;
import com.maru.controller.employment.dto.PendingApprovalRes;
import com.maru.controller.employment.dto.PermissionUpdateReq;
import com.maru.controller.employment.dto.StatusUpdateReq;
import com.maru.security.CurrentUserId;
import com.maru.service.employment.EmploymentQueryService;
import com.maru.service.employment.EmploymentService;
import com.maru.service.tenant.DojangQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/employments")
@RequiredArgsConstructor
public class EmploymentController {

    private final EmploymentService employmentService;
    private final EmploymentQueryService employmentQueryService;
    private final DojangQueryService dojangQueryService;

    /**
     * 승인 요청 API (사범 → 도장)
     *
     * @param userId 현재 인증된 사용자 ID (사범)
     * @param dojangId 승인 요청할 도장 ID
     * @return 생성된 Employment 정보
     */
    @PostMapping("/request")
    public ResponseEntity<EmploymentRes> requestApproval(
            @CurrentUserId String userId,
            @RequestParam String dojangId) {

        EmploymentRes result = employmentService.requestApproval(userId, dojangId);
        return ResponseEntity.ok(result);
    }

    /**
     * 내 승인 요청 목록 조회 (사범용)
     *
     * @param userId 현재 인증된 사용자 ID
     * @return 본인이 보낸 Employment 목록
     */
    @GetMapping("/my-requests")
    public ResponseEntity<List<EmploymentRes>> getMyRequests(@CurrentUserId String userId) {
        List<EmploymentRes> results = employmentQueryService.getMyRequests(userId);
        return ResponseEntity.ok(results);
    }

    /**
     * 대기 중인 승인 요청 목록 조회 (관장용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @return 대기 중인 Employment 목록
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PendingApprovalRes>> getPendingRequests(@CurrentUserId String userId) {
        String dojangId = dojangQueryService.getDojangIdByOwnerId(userId);
        List<PendingApprovalRes> results = employmentQueryService.getPendingRequests(dojangId);
        return ResponseEntity.ok(results);
    }

    /**
     * 승인 처리 API (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @param id 승인할 Employment ID
     * @return 승인된 Employment 정보
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<EmploymentRes> approve(
            @CurrentUserId String userId,
            @PathVariable String id) {

        EmploymentRes result = employmentService.approve(id, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 거절 처리 API (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @param id 거절할 Employment ID
     * @return 거절된 Employment 정보
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<EmploymentRes> reject(
            @CurrentUserId String userId,
            @PathVariable String id) {

        EmploymentRes result = employmentService.reject(id, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 요청 취소 API (사범 본인 전용)
     *
     * @param userId 현재 인증된 사용자 ID (사범)
     * @param id 취소할 Employment ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @CurrentUserId String userId,
            @PathVariable String id) {

        employmentService.cancel(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 사범 목록 조회 (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @return 도장 소속 사범 목록
     */
    @GetMapping("/instructors")
    public ResponseEntity<List<InstructorRes>> getInstructors(@CurrentUserId String userId) {
        String dojangId = dojangQueryService.getDojangIdByOwnerId(userId);
        List<InstructorRes> results = employmentQueryService.getInstructors(dojangId);
        return ResponseEntity.ok(results);
    }

    /**
     * 사범 상세 조회 (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @param id 조회할 Employment ID
     * @return 사범 상세 정보 및 권한 목록
     */
    @GetMapping("/instructors/{id}")
    public ResponseEntity<InstructorDetailRes> getInstructorDetail(
            @CurrentUserId String userId,
            @PathVariable String id) {
        InstructorDetailRes result = employmentQueryService.getInstructorDetail(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 사범 권한 수정 (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @param id 수정할 Employment ID
     * @param req 권한 수정 요청
     * @return 수정된 사범 상세 정보
     */
    @PatchMapping("/instructors/{id}/permissions")
    public ResponseEntity<InstructorDetailRes> updatePermissions(
            @CurrentUserId String userId,
            @PathVariable String id,
            @Valid @RequestBody PermissionUpdateReq req) {
        InstructorDetailRes result = employmentService.updatePermissions(id, req.permissions(), userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 사범 권한 기본값 초기화 (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @param id 초기화할 Employment ID
     * @return 초기화된 사범 상세 정보
     */
    @PostMapping("/instructors/{id}/permissions/reset")
    public ResponseEntity<InstructorDetailRes> resetPermissions(
            @CurrentUserId String userId,
            @PathVariable String id) {
        InstructorDetailRes result = employmentService.resetPermissions(id, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 사범 상태 변경 (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @param id 변경할 Employment ID
     * @param req 상태 변경 요청
     * @return 변경된 사범 상세 정보
     */
    @PatchMapping("/instructors/{id}/status")
    public ResponseEntity<InstructorDetailRes> updateInstructorStatus(
            @CurrentUserId String userId,
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateReq req) {
        InstructorDetailRes result = employmentService.updateInstructorStatus(id, req.status(), req.reason(), userId);
        return ResponseEntity.ok(result);
    }
}
