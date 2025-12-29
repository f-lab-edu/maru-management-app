package com.maru.domain.guardian;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.guardian.exception.GuardianErrorCode;
import com.maru.domain.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guardianship")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guardianship extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GuardianRelation relation;

    private Boolean isPrimary = false;

    private Guardianship(Guardian guardian, Student student, GuardianRelation relation, boolean isPrimary) {
        validateInput(guardian, student);
        this.guardian = guardian;
        this.student = student;
        this.relation = relation;
        this.isPrimary = isPrimary;
    }

    public static Guardianship create(Guardian guardian, Student student, GuardianRelation relation, boolean isPrimary) {
        return new Guardianship(guardian, student, relation, isPrimary);
    }

    public void updatePrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public void updateRelation(GuardianRelation relation) {
        this.relation = relation;
    }

    private void validateInput(Guardian guardian, Student student) {
        DomainAssert.notNull(guardian, GuardianErrorCode.GUARDIAN_REQUIRED);
        DomainAssert.notNull(student, GuardianErrorCode.STUDENT_REQUIRED);
    }
}
