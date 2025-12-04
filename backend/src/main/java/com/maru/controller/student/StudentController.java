package com.maru.controller.student;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.ErrorCode;
import com.maru.controller.student.dto.*;
import com.maru.security.CurrentUserId;
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
            @RequestParam Long dojangId,
            @Valid @RequestBody StudentCreateReq request,
            @CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @Valid @RequestBody StudentUpdateReq request,
            @CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @RequestParam(required = false) String reason,
            @CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
            @PathVariable Long studentId,
            @RequestParam Long dojangId,
            @Valid @RequestBody GuardianCreateReq request,
            @CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
            @PathVariable Long studentId,
            @PathVariable Long guardianId,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
            @PathVariable Long studentId,
            @PathVariable Long guardianId,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
