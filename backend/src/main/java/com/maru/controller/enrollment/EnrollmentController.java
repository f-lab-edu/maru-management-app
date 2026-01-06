package com.maru.controller.enrollment;

import com.maru.controller.enrollment.dto.BulkEnrollmentReq;
import com.maru.controller.enrollment.dto.BulkEnrollmentRes;
import com.maru.controller.enrollment.dto.EnrolledStudentListRes;
import com.maru.controller.enrollment.dto.EnrollStudentReq;
import com.maru.service.enrollment.EnrollmentQueryService;
import com.maru.service.enrollment.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentQueryService enrollmentQueryService;

    /**
     * 원생을 수련반에 등록
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param request 등록할 원생 정보
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Void> enrollStudent(
            @RequestParam String dojangId,
            @RequestParam String divisionId,
            @Valid @RequestBody EnrollStudentReq request
    ) {
        enrollmentService.enrollStudent(dojangId, divisionId, request.studentId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 원생의 수련반 등록 해제
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param studentId 원생 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> unenrollStudent(
            @RequestParam String dojangId,
            @RequestParam String divisionId,
            @PathVariable String studentId
    ) {
        enrollmentService.unenrollStudent(dojangId, divisionId, studentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 여러 원생을 수련반에 일괄 등록
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param request 등록할 원생 ID 목록
     * @return 일괄 등록 결과
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkEnrollmentRes> bulkEnrollStudents(
            @RequestParam String dojangId,
            @RequestParam String divisionId,
            @Valid @RequestBody BulkEnrollmentReq request
    ) {
        BulkEnrollmentRes response = enrollmentService.bulkEnrollStudents(dojangId, divisionId, request.studentIds());
        return ResponseEntity.ok(response);
    }

    /**
     * 수련반에 등록된 원생 목록 조회
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 등록된 원생 목록
     */
    @GetMapping
    public ResponseEntity<EnrolledStudentListRes> getEnrollments(
            @RequestParam String dojangId,
            @RequestParam String divisionId
    ) {
        EnrolledStudentListRes response = enrollmentQueryService.getEnrollments(dojangId, divisionId);
        return ResponseEntity.ok(response);
    }
}
