package com.maru.domain.student;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends SoftDeletableEntity {

    @Column(nullable = false, length = 13)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dojang_id", nullable = false)
    private Dojang dojang;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String photoUrl;

    @Column(length = 20)
    private String phone;

    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status;

    private LocalDate enrolledAt;

    private Student(Dojang dojang, String name, LocalDate birth, String photoUrl, String phone) {
        validateNotNull(dojang, name, birth);
        this.tenantId = dojang.getTenantId();
        this.dojang = dojang;
        this.name = name;
        this.birth = birth;
        this.photoUrl = photoUrl;
        this.phone = phone;
        this.status = StudentStatus.ACTIVE;
        this.enrolledAt = LocalDate.now();
    }

    public static Student create(Dojang dojang, String name, LocalDate birth, String photoUrl, String phone) {
        return new Student(dojang, name, birth, photoUrl, phone);
    }

    public void update(String name, LocalDate birth, String photoUrl, String phone) {
        this.name = name;
        this.birth = birth;
        this.photoUrl = photoUrl;
        this.phone = phone;
    }

    public void changeStatus(StudentStatus status) {
        this.status = status;
    }

    public void withdraw() {
        this.status = StudentStatus.WITHDRAWN;
        markAsDeleted();
    }

    public void reactivate() {
        this.status = StudentStatus.ACTIVE;
        this.enrolledAt = LocalDate.now();
        restore();
    }

    private void validateNotNull(Dojang dojang, String name, LocalDate birth) {
        DomainAssert.notNull(dojang, StudentErrorCode.DOJANG_REQUIRED);
        DomainAssert.hasText(name, StudentErrorCode.NAME_REQUIRED);
        DomainAssert.notNull(birth, StudentErrorCode.BIRTH_REQUIRED);
    }
}
