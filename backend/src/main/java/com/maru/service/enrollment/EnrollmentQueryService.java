package com.maru.service.enrollment;

import com.maru.controller.enrollment.dto.EnrolledStudentListRes;
import com.maru.controller.enrollment.dto.EnrolledStudentRes;
import com.maru.repository.enrollment.EnrollmentRepository;
import com.maru.repository.enrollment.view.EnrollmentStudentView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentQueryService {

    private final EnrollmentRepository enrollmentRepository;

    /**
     * 수련반에 등록된 원생 목록 조회
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 등록된 원생 목록
     */
    public EnrolledStudentListRes getEnrollments(String dojangId, String divisionId) {
        List<EnrollmentStudentView> views = enrollmentRepository
                .findAllWithStudentByDivisionId(dojangId, divisionId);

        List<EnrolledStudentRes> students = views.stream()
                .map(this::toEnrolledStudentRes)
                .toList();

        return EnrolledStudentListRes.from(students);
    }

    /**
     * 특정 수련반의 등록 원생 수 조회
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 등록 원생 수
     */
    public int countByDivisionId(String dojangId, String divisionId) {
        return enrollmentRepository.countByDivisionId(dojangId, divisionId);
    }

    /**
     * 수련부/수련반 필터 조건에 해당하는 원생 ID 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택)
     * @param divisionId 수련반 ID (선택)
     * @return 필터된 원생 ID 목록 (조건 없으면 null → 전체 조회 의도)
     */
    public List<String> getStudentIdsByFilter(String dojangId, String sectionId, String divisionId) {
        if (divisionId != null) {
            return enrollmentRepository.findStudentIdsByDivisionId(dojangId, divisionId);
        }
        if (sectionId != null) {
            return enrollmentRepository.findStudentIdsBySectionId(dojangId, sectionId);
        }
        return null;
    }

    private EnrolledStudentRes toEnrolledStudentRes(EnrollmentStudentView view) {
        return EnrolledStudentRes.builder()
                .studentId(view.getStudentId())
                .studentName(view.getStudentName())
                .enrolledAt(view.getCreatedAt())
                .build();
    }
}
