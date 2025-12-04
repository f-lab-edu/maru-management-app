package com.maru.domain.guardian;

import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(
    name = "guardianship",
    indexes = {
        @Index(name = "idx_guardianship_guardian_id", columnList = "guardian_id"),
        @Index(name = "idx_guardianship_student_id", columnList = "student_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_guardianship_guardian_student", columnNames = {"guardian_id", "student_id"})
    }
)
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

    @Column(name = "is_primary")
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
        Assert.notNull(guardian, "guardian은 필수입니다.");
        Assert.notNull(student, "student는 필수입니다.");
    }
}
