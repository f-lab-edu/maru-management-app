package com.maru.service.student;

import com.maru.common.exception.BusinessException;
import com.maru.controller.student.dto.GuardianRes;
import com.maru.controller.student.dto.StudentListRes;
import com.maru.controller.student.dto.StudentRes;
import com.maru.controller.student.dto.StudentSummaryRes;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.student.view.StudentDetailView;
import com.maru.repository.student.view.StudentSummaryView;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import com.maru.service.enrollment.EnrollmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryService {

    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final EnrollmentQueryService enrollmentQueryService;

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

        boolean hasFilter = sectionId != null || divisionId != null;

        List<StudentSummaryView> views;
        if (hasFilter) {
            List<String> filteredStudentIds = enrollmentQueryService.getStudentIdsByFilter(dojangId, sectionId, divisionId);
            if (filteredStudentIds.isEmpty()) {
                return StudentListRes.empty();
            }
            views = studentRepository.findAllByIdsWithEnrollmentStatus(tenantId, dojangId, filteredStudentIds, StudentStatus.WITHDRAWN);
        } else {
            views = studentRepository.findAllWithEnrollmentStatus(tenantId, dojangId, StudentStatus.WITHDRAWN);
        }

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
    public StudentRes getStudent(String dojangId, String studentId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        StudentDetailView view = studentRepository.findDetailById(studentId, tenantId, StudentStatus.WITHDRAWN)
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
