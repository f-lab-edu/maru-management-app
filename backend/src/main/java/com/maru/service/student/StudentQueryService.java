package com.maru.service.student;

import com.maru.common.exception.BusinessException;
import com.maru.controller.student.dto.GuardianRes;
import com.maru.controller.student.dto.StudentListRes;
import com.maru.controller.student.dto.StudentRes;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.enrollment.EnrollmentRepository;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.student.view.StudentDetailView;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import com.maru.service.enrollment.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryService {

    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    /**
     * 원생 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택)
     * @param divisionId 수련반 ID (선택)
     * @return 원생 목록
     */
    public StudentListRes getStudents(String dojangId, String sectionId, String divisionId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        List<String> filteredStudentIds = enrollmentService.getFilteredStudentIds(dojangId, sectionId, divisionId);

        if (filteredStudentIds != null && filteredStudentIds.isEmpty()) {
            return StudentListRes.empty();
        }

        List<Student> students = (filteredStudentIds != null)
                ? studentRepository.findActiveStudentsByIds(tenantId, dojangId, filteredStudentIds, StudentStatus.WITHDRAWN)
                : studentRepository.findActiveStudents(tenantId, dojangId, StudentStatus.WITHDRAWN);

        Set<String> enrolledStudentIds = Set.of();
        if (!students.isEmpty()) {
            List<String> studentIds = students.stream().map(Student::getId).toList();
            enrolledStudentIds = new HashSet<>(enrollmentRepository.findEnrolledStudentIds(dojangId, studentIds));
        }

        return StudentListRes.from(students, enrolledStudentIds);
    }

    /**
     * 원생 상세 조회
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @return 원생 상세 정보
     * @throws BusinessException NOT_FOUND - 원생을 찾을 수 없음
     */
    public StudentRes getStudent(String dojangId, String studentId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        StudentDetailView view = studentRepository.findViewById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));

        return toStudentRes(view, getGuardianResponses(studentId));
    }

    private void validateDojangAccess(String dojangId, String tenantId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        if (!dojang.getTenantId().equals(tenantId)) {
            throw new BusinessException(DojangErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private List<GuardianRes> getGuardianResponses(String studentId) {
        return guardianshipRepository.findByStudentIdAndDeletedAtIsNull(studentId).stream()
                .map(GuardianRes::from)
                .toList();
    }

    private StudentRes toStudentRes(StudentDetailView view, List<GuardianRes> guardians) {
        return StudentRes.builder()
                .id(view.getId())
                .name(view.getName())
                .birth(view.getBirth())
                .photoUrl(view.getPhotoUrl())
                .phone(view.getPhone())
                .enrolledAt(view.getEnrolledAt())
                .status(view.getStatus())
                .guardians(guardians)
                .build();
    }
}
