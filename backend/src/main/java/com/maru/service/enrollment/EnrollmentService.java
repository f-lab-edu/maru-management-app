package com.maru.service.enrollment;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.CommonErrorCode;
import com.maru.controller.enrollment.dto.BulkEnrollmentRes;
import com.maru.controller.enrollment.dto.EnrolledStudentListRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    /**
     * 원생을 수련반에 등록합니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param studentId 원생 ID
     * @throws BusinessException 수련반/원생을 찾을 수 없거나 이미 등록된 경우
     */
    public void enrollStudent(String dojangId, String divisionId, String studentId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 원생의 수련반 등록을 해제합니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param studentId 원생 ID
     * @throws BusinessException 등록 정보를 찾을 수 없는 경우
     */
    public void unenrollStudent(String dojangId, String divisionId, String studentId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 여러 원생을 수련반에 일괄 등록합니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param studentIds 원생 ID 목록
     * @return 등록 결과 (성공/스킵 수)
     */
    public BulkEnrollmentRes bulkEnrollStudents(String dojangId, String divisionId, List<String> studentIds) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 등록 정보를 조회합니다.
     * divisionId 또는 studentId 중 하나 이상 필수입니다.
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID (optional)
     * @param studentId 원생 ID (optional)
     * @return 등록 목록
     */
    public EnrolledStudentListRes getEnrollments(String dojangId, String divisionId, String studentId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 특정 수련반의 등록 원생 수를 조회합니다.
     *
     * @param divisionId 수련반 ID
     * @return 등록 원생 수
     */
    public int countByDivisionId(String divisionId) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
