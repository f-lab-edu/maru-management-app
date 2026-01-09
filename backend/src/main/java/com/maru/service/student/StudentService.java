package com.maru.service.student;

import com.maru.common.exception.BusinessException;
import com.maru.controller.student.dto.StudentCreateReq;
import com.maru.controller.student.dto.StudentRes;
import com.maru.controller.student.dto.StudentUpdateReq;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;
    private final StudentQueryService queryService;

    /**
     * 원생 등록
     *
     * @param dojangId 도장 ID
     * @param req 원생 등록 정보
     * @param userId 현재 사용자 ID
     * @return 등록된 원생 정보
     * @throws BusinessException DUPLICATE - 이미 등록된 원생 (WITHDRAWN 상태면 재등록)
     */
    @Transactional
    public StudentRes createStudent(String dojangId, StudentCreateReq req, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Optional<Student> existing = studentRepository.findByDojangIdAndNameAndBirth(dojangId, req.name(), req.birth());
        if (existing.isPresent()) {
            Student student = existing.get();
            if (student.getStatus() == StudentStatus.WITHDRAWN) {
                student.reactivate();
                log.info("원생 재등록 - studentId: {}, dojangId: {}", student.getId(), dojangId);
                return queryService.getStudent(dojangId, student.getId());
            }
            throw new BusinessException(StudentErrorCode.DUPLICATE);
        }

        Student student = Student.create(tenantId, dojangId, req.name(), req.birth(), req.photoUrl(), req.phone());
        studentRepository.save(student);
        log.info("원생 등록 - studentId: {}, dojangId: {}", student.getId(), dojangId);

        return queryService.getStudent(dojangId, student.getId());
    }

    /**
     * 원생 정보 수정
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param req 수정할 정보
     * @param userId 현재 사용자 ID
     * @return 수정된 원생 정보
     * @throws BusinessException NOT_FOUND - 원생을 찾을 수 없음
     */
    @Transactional
    public StudentRes updateStudent(String dojangId, String studentId, StudentUpdateReq req, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));

        boolean nameOrBirthChanged = !student.getName().equals(req.name()) || !student.getBirth().equals(req.birth());
        if (nameOrBirthChanged) {
            studentRepository.findByDojangIdAndNameAndBirth(dojangId, req.name(), req.birth())
                    .filter(existing -> !existing.getId().equals(studentId))
                    .ifPresent(existing -> { throw new BusinessException(StudentErrorCode.DUPLICATE); });
        }

        student.update(req.name(), req.birth(), req.photoUrl(), req.phone());

        if (req.status() != null) {
            student.changeStatus(req.status());
        }

        log.info("원생 수정 - studentId: {}, dojangId: {}", studentId, dojangId);
        return queryService.getStudent(dojangId, studentId);
    }

    /**
     * 원생 퇴원 (소프트 삭제)
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param reason 퇴원 사유
     * @param userId 현재 사용자 ID
     * @throws BusinessException NOT_FOUND - 원생을 찾을 수 없음
     */
    @Transactional
    public void deleteStudent(String dojangId, String studentId, String reason, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));

        student.withdraw();
        log.info("원생 퇴원 - studentId: {}, dojangId: {}", studentId, dojangId);
    }

    /**
     * 원생 일괄 퇴원 (소프트 삭제)
     *
     * @param dojangId 도장 ID
     * @param studentIds 원생 ID 목록
     * @param userId 현재 사용자 ID
     * @throws BusinessException NOT_FOUND - 원생을 찾을 수 없음
     */
    @Transactional
    public void bulkDeleteStudents(String dojangId, List<String> studentIds, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        List<Student> students = studentRepository.findAllById(studentIds);

        for (Student student : students) {
            if (!student.getDojangId().equals(dojangId) ||
                !student.getTenantId().equals(tenantId)) {
                throw new BusinessException(DojangErrorCode.UNAUTHORIZED_ACCESS);
            }
            if (student.getStatus() == StudentStatus.WITHDRAWN) {
                throw new BusinessException(StudentErrorCode.NOT_FOUND);
            }
            student.withdraw();
        }

        log.info("원생 일괄 퇴원 - count: {}, dojangId: {}", students.size(), dojangId);
    }

    private void validateDojangAccess(String dojangId, String tenantId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        if (!dojang.getTenantId().equals(tenantId)) {
            throw new BusinessException(DojangErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
