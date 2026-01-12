package com.maru.service.enrollment;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.controller.enrollment.dto.BulkEnrollmentRes;
import com.maru.domain.enrollment.Enrollment;
import com.maru.domain.enrollment.exception.EnrollmentErrorCode;
import com.maru.domain.section.exception.DivisionErrorCode;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.repository.enrollment.EnrollmentRepository;
import com.maru.repository.section.DivisionRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ValidateDojangAccess
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final DivisionRepository divisionRepository;
    private final StudentRepository studentRepository;

    /**
     * 원생을 수련반에 등록합니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param studentId 원생 ID
     * @throws BusinessException 수련반/원생을 찾을 수 없거나 이미 등록된 경우
     */
    @Transactional
    public void enrollStudent(String dojangId, String divisionId, String studentId) {
        validateDivisionExists(divisionId, dojangId);
        validateStudentExists(studentId);
        validateNotAlreadyEnrolled(dojangId, divisionId, studentId);

        Enrollment enrollment = Enrollment.create(dojangId, divisionId, studentId);
        enrollmentRepository.save(enrollment);
    }

    /**
     * 원생의 수련반 등록을 해제합니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param studentId 원생 ID
     * @throws BusinessException 등록 정보를 찾을 수 없는 경우
     */
    @Transactional
    public void unenrollStudent(String dojangId, String divisionId, String studentId) {
        Enrollment enrollment = enrollmentRepository
                .findByDojangIdAndDivisionIdAndStudentId(dojangId, divisionId, studentId)
                .orElseThrow(() -> new BusinessException(EnrollmentErrorCode.NOT_ENROLLED));

        enrollmentRepository.delete(enrollment);
    }

    /**
     * 여러 원생을 수련반에 일괄 등록합니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param studentIds 원생 ID 목록
     * @return 등록 결과 (성공/스킵 수)
     */
    @Transactional
    public BulkEnrollmentRes bulkEnrollStudents(String dojangId, String divisionId, List<String> studentIds) {
        validateDivisionExists(divisionId, dojangId);

        Set<String> alreadyEnrolledIds = findAlreadyEnrolledStudentIds(dojangId, divisionId, studentIds);

        List<String> toEnrollIds = studentIds.stream()
                .filter(id -> !alreadyEnrolledIds.contains(id))
                .toList();

        String tenantId = TenantContextHolder.getTenantId();
        List<String> activeStudentIds = studentRepository.findActiveStudentIds(
                toEnrollIds, tenantId, StudentStatus.WITHDRAWN);

        List<Enrollment> enrollments = new ArrayList<>();
        for (String studentId : activeStudentIds) {
            enrollments.add(Enrollment.create(dojangId, divisionId, studentId));
        }
        enrollmentRepository.saveAll(enrollments);

        List<String> skippedStudentIds = new ArrayList<>(alreadyEnrolledIds);

        return BulkEnrollmentRes.of(enrollments.size(), skippedStudentIds);
    }

    private void validateDivisionExists(String divisionId, String dojangId) {
        if (!divisionRepository.findByIdAndDojangIdWithSection(divisionId, dojangId).isPresent()) {
            throw new BusinessException(DivisionErrorCode.NOT_FOUND);
        }
    }

    private void validateStudentExists(String studentId) {
        String tenantId = TenantContextHolder.getTenantId();
        if (!studentRepository.existsActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)) {
            throw new BusinessException(StudentErrorCode.NOT_FOUND);
        }
    }

    private void validateNotAlreadyEnrolled(String dojangId, String divisionId, String studentId) {
        if (enrollmentRepository.existsByDojangIdAndDivisionIdAndStudentId(dojangId, divisionId, studentId)) {
            throw new BusinessException(EnrollmentErrorCode.ALREADY_ENROLLED);
        }
    }

    private Set<String> findAlreadyEnrolledStudentIds(String dojangId, String divisionId, List<String> studentIds) {
        return new HashSet<>(enrollmentRepository.findAlreadyEnrolledStudentIds(dojangId, divisionId, studentIds));
    }
}
