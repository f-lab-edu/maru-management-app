package com.maru.service.enrollment;

import com.maru.common.exception.BusinessException;
import com.maru.controller.enrollment.dto.BulkEnrollmentRes;
import com.maru.controller.enrollment.dto.EnrolledStudentListRes;
import com.maru.controller.enrollment.dto.EnrolledStudentRes;
import com.maru.domain.division.Division;
import com.maru.domain.division.exception.DivisionErrorCode;
import com.maru.domain.enrollment.Enrollment;
import com.maru.common.exception.EnrollmentErrorCode;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.repository.division.DivisionRepository;
import com.maru.repository.enrollment.EnrollmentRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
        Division division = findDivisionByIdAndDojangId(divisionId, dojangId);
        Student student = findActiveStudentById(studentId);

        validateNotAlreadyEnrolled(dojangId, divisionId, studentId);

        Enrollment enrollment = Enrollment.create(division, student);
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
        Division division = findDivisionByIdAndDojangId(divisionId, dojangId);

        Set<String> alreadyEnrolledIds = findAlreadyEnrolledStudentIds(dojangId, divisionId, studentIds);

        List<String> toEnrollIds = studentIds.stream()
                .filter(id -> !alreadyEnrolledIds.contains(id))
                .toList();

        String tenantId = TenantContextHolder.getTenantId();
        List<Student> studentsToEnroll = studentRepository.findAllActiveByIds(
                toEnrollIds, tenantId, StudentStatus.WITHDRAWN);

        List<Enrollment> enrollments = new ArrayList<>();
        for (Student student : studentsToEnroll) {
            enrollments.add(Enrollment.create(division, student));
        }
        enrollmentRepository.saveAll(enrollments);

        List<String> skippedStudentIds = new ArrayList<>(alreadyEnrolledIds);

        return BulkEnrollmentRes.builder()
                .enrolledCount(enrollments.size())
                .skippedCount(skippedStudentIds.size())
                .skippedStudentIds(skippedStudentIds)
                .build();
    }

    /**
     * 수련반에 등록된 원생 목록을 조회합니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 등록된 원생 목록
     */
    public EnrolledStudentListRes getEnrollments(String dojangId, String divisionId) {
        List<Enrollment> enrollments = enrollmentRepository
                .findAllWithStudentByDivisionId(dojangId, divisionId);

        List<EnrolledStudentRes> students = enrollments.stream()
                .map(EnrolledStudentRes::from)
                .toList();

        return EnrolledStudentListRes.from(students);
    }

    /**
     * 특정 수련반의 등록 원생 수를 조회합니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 등록 원생 수
     */
    public int countByDivisionId(String dojangId, String divisionId) {
        return enrollmentRepository.countByDivisionId(dojangId, divisionId);
    }

    private Division findDivisionByIdAndDojangId(String divisionId, String dojangId) {
        return divisionRepository.findByIdAndDojangIdWithSection(divisionId, dojangId)
                .orElseThrow(() -> new BusinessException(DivisionErrorCode.NOT_FOUND));
    }

    private Student findActiveStudentById(String studentId) {
        String tenantId = TenantContextHolder.getTenantId();
        return studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));
    }

    private void validateNotAlreadyEnrolled(String dojangId, String divisionId, String studentId) {
        if (enrollmentRepository.existsByDojangIdAndDivisionIdAndStudentId(dojangId, divisionId, studentId)) {
            throw new BusinessException(EnrollmentErrorCode.ALREADY_ENROLLED);
        }
    }

    private Set<String> findAlreadyEnrolledStudentIds(String dojangId, String divisionId, List<String> studentIds) {
        return enrollmentRepository.findAlreadyEnrolledStudentIds(dojangId, divisionId, studentIds)
                .stream()
                .collect(Collectors.toSet());
    }
}
