package com.maru.domain.enrollment;

import com.maru.common.exception.DomainAssert;
import com.maru.common.exception.EnrollmentErrorCode;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.division.Division;
import com.maru.domain.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enrollment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends SoftDeletableEntity {

    @Column(nullable = false, length = 13)
    private String dojangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private Enrollment(String dojangId, Division division, Student student) {
        this.dojangId = dojangId;
        this.division = division;
        this.student = student;
    }

    public static Enrollment create(Division division, Student student) {
        DomainAssert.notNull(division, EnrollmentErrorCode.DIVISION_REQUIRED);
        DomainAssert.notNull(student, EnrollmentErrorCode.STUDENT_REQUIRED);
        String dojangId = division.getDojang().getId();
        return new Enrollment(dojangId, division, student);
    }
}
