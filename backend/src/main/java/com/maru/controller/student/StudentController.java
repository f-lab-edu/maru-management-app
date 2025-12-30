package com.maru.controller.student;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.CommonErrorCode;
import com.maru.controller.student.dto.*;
import com.maru.security.CurrentUserId;
import com.maru.service.guardian.GuardianService;
import com.maru.service.student.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 원생 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final GuardianService guardianService;

    /**
     * 원생 등록
     *
     * @param dojangId 도장 ID
     * @param request 원생 등록 정보
     * @param userId 현재 사용자 ID
     * @return 등록된 원생 정보
     */
    @PostMapping
    public ResponseEntity<StudentRes> createStudent(
            @RequestParam String dojangId,
            @Valid @RequestBody StudentCreateReq request,
            @CurrentUserId String userId) {
        StudentRes response = studentService.createStudent(dojangId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 원생 목록 조회
     *
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 원생 목록 (enrolled_at DESC, 최대 200건)
     */
    @GetMapping
    public ResponseEntity<StudentListRes> getStudents(
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        StudentListRes response = studentService.getStudents(dojangId);
        return ResponseEntity.ok(response);
    }

    /**
     * 원생 상세 조회
     *
     * @param id 원생 ID
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 원생 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentRes> getStudent(
            @PathVariable String id,
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        StudentRes response = studentService.getStudent(dojangId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * 원생 정보 수정
     *
     * @param id 원생 ID
     * @param dojangId 도장 ID
     * @param request 수정할 정보
     * @param userId 현재 사용자 ID
     * @return 수정된 원생 정보
     */
    @PatchMapping("/{id}")
    public ResponseEntity<StudentRes> updateStudent(
            @PathVariable String id,
            @RequestParam String dojangId,
            @Valid @RequestBody StudentUpdateReq request,
            @CurrentUserId String userId) {
        StudentRes response = studentService.updateStudent(dojangId, id, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 원생 삭제 (소프트 삭제 - WITHDRAWN 상태 변경)
     *
     * @param id 원생 ID
     * @param dojangId 도장 ID
     * @param reason 삭제 사유 (선택)
     * @param userId 현재 사용자 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable String id,
            @RequestParam String dojangId,
            @RequestParam(required = false) String reason,
            @CurrentUserId String userId) {
        studentService.deleteStudent(dojangId, id, reason, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 원생 일괄 삭제 (소프트 삭제 - WITHDRAWN 상태 변경)
     *
     * @param dojangId 도장 ID
     * @param request 삭제할 원생 ID 목록
     * @param userId 현재 사용자 ID
     * @return 204 No Content
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDeleteStudents(
            @RequestParam String dojangId,
            @Valid @RequestBody BulkDeleteReq request,
            @CurrentUserId String userId) {
        studentService.bulkDeleteStudents(dojangId, request.studentIds(), userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 보호자 연결 또는 생성
     *
     * @param studentId 원생 ID
     * @param dojangId 도장 ID
     * @param request 보호자 정보
     * @param userId 현재 사용자 ID
     * @return 연결된 보호자 정보
     */
    @PostMapping("/{studentId}/guardians")
    public ResponseEntity<GuardianRes> addGuardian(
            @PathVariable String studentId,
            @RequestParam String dojangId,
            @Valid @RequestBody GuardianCreateReq request,
            @CurrentUserId String userId) {
        GuardianRes response = guardianService.addGuardian(dojangId, studentId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 보호자 정보 수정
     *
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @param dojangId 도장 ID
     * @param request 수정할 보호자 정보
     * @param userId 현재 사용자 ID
     * @return 수정된 보호자 정보
     */
    @PatchMapping("/{studentId}/guardians/{guardianId}")
    public ResponseEntity<GuardianRes> updateGuardian(
            @PathVariable String studentId,
            @PathVariable String guardianId,
            @RequestParam String dojangId,
            @Valid @RequestBody GuardianUpdateReq request,
            @CurrentUserId String userId) {
        GuardianRes response = guardianService.updateGuardian(dojangId, studentId, guardianId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 주 보호자 설정
     *
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 204 No Content
     */
    @PatchMapping("/{studentId}/guardians/{guardianId}/primary")
    public ResponseEntity<Void> setPrimaryGuardian(
            @PathVariable String studentId,
            @PathVariable String guardianId,
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        guardianService.setPrimaryGuardian(dojangId, studentId, guardianId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 보호자 삭제
     *
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{studentId}/guardians/{guardianId}")
    public ResponseEntity<Void> removeGuardian(
            @PathVariable String studentId,
            @PathVariable String guardianId,
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        guardianService.removeGuardian(dojangId, studentId, guardianId);
        return ResponseEntity.noContent().build();
    }

    /**
     * SMS 초대 발송 (Stub - MVP 2차 구현 예정)
     *
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @param dojangId 도장 ID
     * @param userId 현재 사용자 ID
     * @return 204 No Content
     */
    @PostMapping("/{studentId}/guardians/{guardianId}/invite-sms")
    public ResponseEntity<Void> sendInviteSms(
            @PathVariable String studentId,
            @PathVariable String guardianId,
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
