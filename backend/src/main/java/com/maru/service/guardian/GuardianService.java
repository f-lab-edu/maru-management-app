package com.maru.service.guardian;

import com.maru.common.exception.BusinessException;
import com.maru.controller.student.dto.GuardianCreateReq;
import com.maru.controller.student.dto.GuardianRes;
import com.maru.domain.guardian.Guardian;
import com.maru.domain.guardian.Guardianship;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.tenant.Dojang;
import com.maru.repository.guardian.GuardianRepository;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;

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
    public GuardianRes addGuardian(Long dojangId, Long studentId, GuardianCreateReq req) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(STUDENT_NOT_FOUND));

        validateStudentBelongsToDojang(student, dojangId);

        Guardian guardian = findOrCreateGuardian(req.phone(), req.name());

        validateGuardianshipNotExists(studentId, guardian.getId());

        if (req.isPrimary()) {
            clearExistingPrimaryGuardian(studentId);
        }

        Guardianship guardianship = guardianshipRepository.save(
                Guardianship.create(guardian, student, req.relation(), req.isPrimary())
        );

        log.info("보호자 연결 - studentId: {}, guardianId: {}", studentId, guardian.getId());
        return GuardianRes.from(guardianship);
    }

    /**
     * 주 보호자 설정
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     * @throws BusinessException GUARDIAN_NOT_FOUND - 보호자를 찾을 수 없음
     */
    @Transactional
    public void setPrimaryGuardian(Long dojangId, Long studentId, Long guardianId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(STUDENT_NOT_FOUND));

        validateStudentBelongsToDojang(student, dojangId);

        clearExistingPrimaryGuardian(studentId);

        Guardianship target = guardianshipRepository.findByStudentIdAndGuardianIdAndDeletedAtIsNull(studentId, guardianId)
                .orElseThrow(() -> new BusinessException(GUARDIAN_NOT_FOUND));
        target.updatePrimary(true);

        log.info("주 보호자 변경 - studentId: {}, guardianId: {}", studentId, guardianId);
    }

    /**
     * 보호자 목록 조회
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @return 보호자 목록
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     */
    @Transactional(readOnly = true)
    public List<GuardianRes> getGuardians(Long dojangId, Long studentId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(STUDENT_NOT_FOUND));

        validateStudentBelongsToDojang(student, dojangId);

        return guardianshipRepository.findByStudentIdAndDeletedAtIsNull(studentId).stream()
                .map(GuardianRes::from)
                .toList();
    }

    private Guardian findOrCreateGuardian(String phone, String name) {
        return guardianRepository.findByPhoneAndDeletedAtIsNull(phone)
                .orElseGet(() -> guardianRepository.save(Guardian.create(phone, name)));
    }

    private void validateDojangAccess(Long dojangId, Long tenantId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DOJANG_NOT_FOUND));

        if (!dojang.getTenant().getId().equals(tenantId)) {
            throw new BusinessException(UNAUTHORIZED_DOJANG_ACCESS);
        }
    }

    private void validateStudentBelongsToDojang(Student student, Long dojangId) {
        if (!student.getDojang().getId().equals(dojangId)) {
            throw new BusinessException(STUDENT_NOT_FOUND);
        }
    }

    private void validateGuardianshipNotExists(Long studentId, Long guardianId) {
        guardianshipRepository.findByStudentIdAndGuardianIdAndDeletedAtIsNull(studentId, guardianId)
                .ifPresent(gs -> {
                    throw new BusinessException(GUARDIANSHIP_ALREADY_EXISTS);
                });
    }

    private void clearExistingPrimaryGuardian(Long studentId) {
        guardianshipRepository.findByStudentIdAndIsPrimaryTrueAndDeletedAtIsNull(studentId)
                .ifPresent(gs -> gs.updatePrimary(false));
    }
}
