package com.maru.domain.attendance;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.DomainAssert;
import com.maru.domain.attendance.exception.AttendanceErrorCode;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "attendance",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_student_date",
        columnNames = {"tenant_id", "student_id", "attendance_date"}
    ),
    indexes = {
        @Index(name = "idx_attendance_tenant_date", columnList = "tenant_id, attendance_date"),
        @Index(name = "idx_attendance_student_date", columnList = "student_id, attendance_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "dojang_id", nullable = false)
    private Long dojangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "checked_by")
    private Long checkedBy;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private CheckMethod method;

    @Column(name = "checkin_at", nullable = false)
    private LocalDateTime checkinAt;

    @Column(name = "checkout_at")
    private LocalDateTime checkoutAt;

    @Column(name = "note", length = 500)
    private String note;

    private Attendance(Student student, CheckMethod method, LocalDateTime checkinAt, String note) {
        validateCommonRules(student);
        DomainAssert.notNull(method, AttendanceErrorCode.METHOD_REQUIRED);
        DomainAssert.notNull(checkinAt, AttendanceErrorCode.CHECKIN_AT_REQUIRED);

        this.tenantId = student.getTenantId();
        this.dojangId = student.getDojang().getId();
        this.student = student;
        this.attendanceDate = checkinAt.toLocalDate();
        this.status = AttendanceStatus.PRESENT;
        this.method = method;
        this.checkinAt = checkinAt;
        this.note = note;
    }

    private Attendance(Student student, LocalDate date, CheckMethod method) {
        validateCommonRules(student);
        DomainAssert.notNull(date, AttendanceErrorCode.DATE_REQUIRED);

        this.tenantId = student.getTenantId();
        this.dojangId = student.getDojang().getId();
        this.student = student;
        this.attendanceDate = date;
        this.status = AttendanceStatus.ABSENT;
        this.method = method;
        this.checkinAt = date.atStartOfDay();
    }

    public static Attendance create(Student student, CheckMethod method, LocalDateTime checkinAt, String note) {
        return new Attendance(student, method, checkinAt, note);
    }

    public static Attendance createAutoAbsent(Student student, LocalDate date) {
        return new Attendance(student, date, CheckMethod.AUTO);
    }

    public void checkOut(LocalDateTime checkoutAt) {
        DomainAssert.notNull(checkoutAt, AttendanceErrorCode.CHECKOUT_AT_REQUIRED);
        if (this.checkoutAt != null) {
            throw new BusinessException(AttendanceErrorCode.ALREADY_CHECKOUT);
        }
        if (checkoutAt.isBefore(this.checkinAt)) {
            throw new BusinessException(AttendanceErrorCode.CHECKOUT_BEFORE_CHECKIN);
        }
        this.checkoutAt = checkoutAt;
    }

    public void changeStatus(AttendanceStatus newStatus, String note) {
        DomainAssert.notNull(newStatus, AttendanceErrorCode.STATUS_REQUIRED);

        if (this.status == newStatus) {
            throw new BusinessException(AttendanceErrorCode.SAME_STATUS);
        }

        this.status = newStatus;
        if (note != null) {
            this.note = note;
        }
    }

    private void validateCommonRules(Student student) {
        DomainAssert.notNull(student, AttendanceErrorCode.STUDENT_REQUIRED);
    }
}
