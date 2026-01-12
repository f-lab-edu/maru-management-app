package com.maru.domain.enrollment;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.enrollment.exception.EnrollmentErrorCode;
import com.maru.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enrollment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends BaseEntity {

    @Column(nullable = false, length = 13)
    private String dojangId;

    @Column(name = "division_id", nullable = false)
    private String divisionId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    private Enrollment(String dojangId, String divisionId, String studentId) {
        this.dojangId = dojangId;
        this.divisionId = divisionId;
        this.studentId = studentId;
    }

    public static Enrollment create(String dojangId, String divisionId, String studentId) {
        DomainAssert.hasText(dojangId, EnrollmentErrorCode.DOJANG_REQUIRED);
        DomainAssert.hasText(divisionId, EnrollmentErrorCode.DIVISION_REQUIRED);
        DomainAssert.hasText(studentId, EnrollmentErrorCode.STUDENT_REQUIRED);
        return new Enrollment(dojangId, divisionId, studentId);
    }
}
