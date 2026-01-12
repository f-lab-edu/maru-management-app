package com.maru.service.guardian;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.controller.student.dto.GuardianCreateReq;
import com.maru.controller.student.dto.GuardianRes;
import com.maru.controller.student.dto.GuardianUpdateReq;
import com.maru.domain.guardian.Guardian;
import com.maru.domain.guardian.Guardianship;
import com.maru.domain.guardian.exception.GuardianErrorCode;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.repository.guardian.GuardianRepository;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ValidateDojangAccess
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final StudentRepository studentRepository;
    private final GuardianQueryService guardianQueryService;

    /**
     * 보호자 추가
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param req 보호자 정보
     * @return 연결된 보호자 정보
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     * @throws BusinessException GUARDIANSHIP_ALREADY_EXISTS - 이미 연결된 보호자
     */
    @Transactional
    public GuardianRes addGuardian(String dojangId, String studentId, GuardianCreateReq req) {
        String tenantId = TenantContextHolder.getTenantId();
        validateStudentInDojang(studentId, tenantId, dojangId);

        Guardian guardian = findOrCreateGuardian(req.phone(), req.name());

        validateGuardianshipNotExists(studentId, guardian.getId());

        guardianshipRepository.save(
                Guardianship.create(guardian, studentId, req.relation(), req.isPrimary())
        );

        log.info("보호자 연결 - studentId: {}, guardianId: {}", studentId, guardian.getId());
        return guardianQueryService.getGuardian(studentId, guardian.getId());
    }

    /**
     * 보호자 정보 수정
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @param req 수정할 정보
     * @return 수정된 보호자 정보
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     * @throws BusinessException GUARDIAN_NOT_FOUND - 보호자를 찾을 수 없음
     */
    @Transactional
    public GuardianRes updateGuardian(String dojangId, String studentId, String guardianId, GuardianUpdateReq req) {
        String tenantId = TenantContextHolder.getTenantId();
        validateStudentInDojang(studentId, tenantId, dojangId);

        Guardianship guardianship = guardianshipRepository.findByStudentIdAndGuardianIdAndDeletedAtIsNull(studentId, guardianId)
                .orElseThrow(() -> new BusinessException(GuardianErrorCode.NOT_FOUND));

        Guardian guardian = guardianship.getGuardian();
        guardian.updateName(req.name());
        guardian.updatePhone(req.phone());
        guardianship.updateRelation(req.relation());

        log.info("보호자 정보 수정 - studentId: {}, guardianId: {}", studentId, guardianId);
        return guardianQueryService.getGuardian(studentId, guardianId);
    }

    /**
     * 주 보호자 설정 (토글)
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     * @throws BusinessException GUARDIAN_NOT_FOUND - 보호자를 찾을 수 없음
     */
    @Transactional
    public void setPrimaryGuardian(String dojangId, String studentId, String guardianId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateStudentInDojang(studentId, tenantId, dojangId);

        Guardianship target = guardianshipRepository.findByStudentIdAndGuardianIdAndDeletedAtIsNull(studentId, guardianId)
                .orElseThrow(() -> new BusinessException(GuardianErrorCode.NOT_FOUND));

        boolean newPrimaryStatus = !target.getIsPrimary();
        target.updatePrimary(newPrimaryStatus);

        log.info("주 보호자 {} - studentId: {}, guardianId: {}", newPrimaryStatus ? "설정" : "해제", studentId, guardianId);
    }

    /**
     * 보호자 삭제
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     * @throws BusinessException GUARDIAN_NOT_FOUND - 보호자 연결을 찾을 수 없음
     */
    @Transactional
    public void removeGuardian(String dojangId, String studentId, String guardianId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateStudentInDojang(studentId, tenantId, dojangId);

        Guardianship guardianship = guardianshipRepository.findByStudentIdAndGuardianIdAndDeletedAtIsNull(studentId, guardianId)
                .orElseThrow(() -> new BusinessException(GuardianErrorCode.NOT_FOUND));

        guardianship.markAsDeleted();

        log.info("보호자 삭제 - studentId: {}, guardianId: {}", studentId, guardianId);
    }

    private Guardian findOrCreateGuardian(String phone, String name) {
        return guardianRepository.findByPhoneAndDeletedAtIsNull(phone)
                .orElseGet(() -> guardianRepository.save(Guardian.create(phone, name)));
    }

    private void validateStudentInDojang(String studentId, String tenantId, String dojangId) {
        if (!studentRepository.existsActiveByIdAndDojangId(studentId, tenantId, dojangId, StudentStatus.WITHDRAWN)) {
            throw new BusinessException(StudentErrorCode.NOT_FOUND);
        }
    }

    private void validateGuardianshipNotExists(String studentId, String guardianId) {
        guardianshipRepository.findByStudentIdAndGuardianIdAndDeletedAtIsNull(studentId, guardianId)
                .ifPresent(gs -> {
                    throw new BusinessException(GuardianErrorCode.GUARDIANSHIP_ALREADY_EXISTS);
                });
    }
}
