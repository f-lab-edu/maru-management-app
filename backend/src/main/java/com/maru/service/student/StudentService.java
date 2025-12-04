package com.maru.service.student;

import com.maru.common.exception.BusinessException;
import com.maru.controller.student.dto.*;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.tenant.Dojang;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;
    private final GuardianshipRepository guardianshipRepository;

    /**
     * 원생 등록
     *
     * @param dojangId 도장 ID
     * @param req 원생 등록 정보
     * @param userId 현재 사용자 ID
     * @return 등록된 원생 정보
     * @throws BusinessException STUDENT_DUPLICATE - 이미 등록된 원생 (WITHDRAWN 상태면 재등록)
     */
    @Transactional
    public StudentRes createStudent(Long dojangId, StudentCreateReq req, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        Dojang dojang = validateDojangAccess(dojangId, tenantId);

        Optional<Student> existing = studentRepository.findByDojangIdAndNameAndBirth(dojangId, req.name(), req.birth());
        if (existing.isPresent()) {
            Student student = existing.get();
            if (student.getStatus() == StudentStatus.WITHDRAWN) {
                student.reactivate();
                log.info("원생 재등록 - studentId: {}, dojangId: {}", student.getId(), dojangId);
                return StudentRes.from(student, getGuardianResponses(student.getId()));
            }
            throw new BusinessException(STUDENT_DUPLICATE);
        }

        Student student = Student.create(dojang, req.name(), req.birth(), req.photoUrl(), req.phone());
        studentRepository.save(student);
        log.info("원생 등록 - studentId: {}, dojangId: {}", student.getId(), dojangId);

        return StudentRes.from(student);
    }

    /**
     * 원생 목록 조회
     *
     * @param dojangId 도장 ID
     * @return 원생 목록 (enrolled_at DESC)
     */
    @Transactional(readOnly = true)
    public StudentListRes getStudents(Long dojangId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        List<Student> students = studentRepository.findActiveStudents(tenantId, dojangId, StudentStatus.WITHDRAWN);
        return StudentListRes.from(students);
    }

    /**
     * 원생 상세 조회
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @return 원생 상세 정보
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     */
    @Transactional(readOnly = true)
    public StudentRes getStudent(Long dojangId, Long studentId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(STUDENT_NOT_FOUND));

        return StudentRes.from(student, getGuardianResponses(studentId));
    }

    /**
     * 원생 정보 수정
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param req 수정할 정보
     * @param userId 현재 사용자 ID
     * @return 수정된 원생 정보
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     */
    @Transactional
    public StudentRes updateStudent(Long dojangId, Long studentId, StudentUpdateReq req, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(STUDENT_NOT_FOUND));

        student.update(req.name(), req.birth(), req.photoUrl(), req.phone());

        if (req.status() != null) {
            student.changeStatus(req.status());
        }

        log.info("원생 수정 - studentId: {}, dojangId: {}", studentId, dojangId);
        return StudentRes.from(student, getGuardianResponses(studentId));
    }

    /**
     * 원생 퇴원 (소프트 삭제)
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param reason 퇴원 사유
     * @param userId 현재 사용자 ID
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     */
    @Transactional
    public void deleteStudent(Long dojangId, Long studentId, String reason, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(STUDENT_NOT_FOUND));

        student.withdraw();
        log.info("원생 퇴원 - studentId: {}, dojangId: {}", studentId, dojangId);
    }

    private Dojang validateDojangAccess(Long dojangId, Long tenantId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DOJANG_NOT_FOUND));

        if (!dojang.getTenant().getId().equals(tenantId)) {
            throw new BusinessException(UNAUTHORIZED_DOJANG_ACCESS);
        }

        return dojang;
    }

    private List<GuardianRes> getGuardianResponses(Long studentId) {
        return guardianshipRepository.findByStudentIdAndDeletedAtIsNull(studentId).stream()
                .map(GuardianRes::from)
                .toList();
    }
}
