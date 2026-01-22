package com.maru.service.student;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.domain.permission.PermissionType;
import com.maru.security.RequirePermission;
import com.maru.controller.student.dto.GuardianRes;
import com.maru.controller.student.dto.StudentListRes;
import com.maru.controller.student.dto.StudentRes;
import com.maru.controller.student.dto.StudentSummaryRes;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.student.view.StudentDetailView;
import com.maru.repository.student.view.StudentSummaryView;
import com.maru.security.TenantContextHolder;
import com.maru.service.enrollment.EnrollmentQueryService;
import com.maru.service.guardian.GuardianQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ValidateDojangAccess
public class StudentQueryService {

    private final StudentRepository studentRepository;
    private final EnrollmentQueryService enrollmentQueryService;
    private final GuardianQueryService guardianQueryService;

    /**
     * 원생 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택)
     * @param divisionId 수련반 ID (선택)
     * @param includeWithdrawn 퇴원 원생 포함 여부
     * @return 원생 목록
     */
    @RequirePermission(PermissionType.STUDENT_VIEW)
    public StudentListRes getStudents(String dojangId, String sectionId, String divisionId, Boolean includeWithdrawn) {
        List<StudentSummaryView> views = findStudentViews(dojangId, sectionId, divisionId, includeWithdrawn);
        return buildStudentListRes(views);
    }

    private List<StudentSummaryView> findStudentViews(String dojangId, String sectionId, String divisionId, Boolean includeWithdrawn) {
        String tenantId = TenantContextHolder.getTenantId();

        if (hasEnrollmentFilter(sectionId, divisionId)) {
            return findByEnrollmentFilter(tenantId, dojangId, sectionId, divisionId);
        }
        return findAll(tenantId, dojangId, includeWithdrawn);
    }

    private boolean hasEnrollmentFilter(String sectionId, String divisionId) {
        return sectionId != null || divisionId != null;
    }

    private List<StudentSummaryView> findByEnrollmentFilter(String tenantId, String dojangId, String sectionId, String divisionId) {
        List<String> studentIds = enrollmentQueryService.getStudentIdsByFilter(dojangId, sectionId, divisionId);
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return studentRepository.findAllByIdsWithEnrollmentStatus(tenantId, dojangId, studentIds, StudentStatus.WITHDRAWN);
    }

    private List<StudentSummaryView> findAll(String tenantId, String dojangId, Boolean includeWithdrawn) {
        if (Boolean.TRUE.equals(includeWithdrawn)) {
            return studentRepository.findAllWithEnrollmentStatusIncludingDeleted(tenantId, dojangId);
        }
        return studentRepository.findAllWithEnrollmentStatus(tenantId, dojangId, StudentStatus.WITHDRAWN);
    }

    private StudentListRes buildStudentListRes(List<StudentSummaryView> views) {
        List<StudentSummaryRes> summaries = views.stream()
                .map(this::toStudentSummaryRes)
                .toList();

        return StudentListRes.builder()
                .students(summaries)
                .totalCount(summaries.size())
                .returnedCount(summaries.size())
                .hasMore(false)
                .build();
    }

    /**
     * 원생 상세 조회
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @return 원생 상세 정보
     * @throws BusinessException NOT_FOUND - 원생을 찾을 수 없음
     */
    @RequirePermission(PermissionType.STUDENT_VIEW)
    public StudentRes getStudent(String dojangId, String studentId) {
        String tenantId = TenantContextHolder.getTenantId();

        StudentDetailView view = studentRepository.findDetailById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));

        return toStudentRes(view, getGuardianResponses(studentId));
    }

    private List<GuardianRes> getGuardianResponses(String studentId) {
        return guardianQueryService.getGuardiansByStudentId(studentId);
    }

    private StudentSummaryRes toStudentSummaryRes(StudentSummaryView view) {
        return StudentSummaryRes.builder()
                .id(view.getId())
                .name(view.getName())
                .birth(view.getBirth())
                .photoUrl(view.getPhotoUrl())
                .enrolledAt(view.getEnrolledAt())
                .status(view.getStatus())
                .hasEnrollment(Boolean.TRUE.equals(view.getHasEnrollment()))
                .build();
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
