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
@Table(name = "attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseEntity {

    @Column(nullable = false, length = 13)
    private String tenantId;

    @Column(nullable = false, length = 13)
    private String dojangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(length = 13)
    private String checkedBy;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckMethod method;

    @Column(nullable = false)
    private LocalDateTime checkinAt;

    private LocalDateTime checkoutAt;

    @Column(length = 500)
    private String note;

    private Attendance(Student student, CheckMethod method, AttendanceStatus status, LocalDateTime checkinAt, String note) {
        validateCommonRules(student);
        DomainAssert.notNull(method, AttendanceErrorCode.METHOD_REQUIRED);
        DomainAssert.notNull(status, AttendanceErrorCode.STATUS_REQUIRED);

        this.tenantId = student.getTenantId();
        this.dojangId = student.getDojangId();
        this.student = student;
        this.status = status;
        this.method = method;
        this.note = note;

        if (status == AttendanceStatus.PRESENT) {
            DomainAssert.notNull(checkinAt, AttendanceErrorCode.CHECKIN_AT_REQUIRED);
            this.checkinAt = checkinAt;
            this.attendanceDate = checkinAt.toLocalDate();
        } else {
            this.checkinAt = checkinAt != null ? checkinAt : LocalDateTime.now().toLocalDate().atStartOfDay();
            this.attendanceDate = this.checkinAt.toLocalDate();
        }
    }

    private Attendance(Student student, LocalDate date, CheckMethod method) {
        validateCommonRules(student);
        DomainAssert.notNull(date, AttendanceErrorCode.DATE_REQUIRED);

        this.tenantId = student.getTenantId();
        this.dojangId = student.getDojangId();
        this.student = student;
        this.attendanceDate = date;
        this.status = AttendanceStatus.ABSENT;
        this.method = method;
        this.checkinAt = date.atStartOfDay();
    }

    public static Attendance create(Student student, CheckMethod method, AttendanceStatus status, LocalDateTime checkinAt, String note) {
        return new Attendance(student, method, status, checkinAt, note);
    }

    public static Attendance create(Student student, CheckMethod method, LocalDateTime checkinAt, String note) {
        return new Attendance(student, method, AttendanceStatus.PRESENT, checkinAt, note);
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

    public void cancelCheckout() {
        if (this.checkoutAt == null) {
            throw new BusinessException(AttendanceErrorCode.NOT_CHECKOUT);
        }
        this.checkoutAt = null;
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

    public void changeTime(LocalDateTime newCheckinAt, LocalDateTime newCheckoutAt) {
        if (newCheckinAt != null) {
            this.checkinAt = newCheckinAt;
            this.attendanceDate = newCheckinAt.toLocalDate();
        }
        if (newCheckoutAt != null) {
            if (this.checkinAt != null && newCheckoutAt.isBefore(this.checkinAt)) {
                throw new BusinessException(AttendanceErrorCode.CHECKOUT_BEFORE_CHECKIN);
            }
            this.checkoutAt = newCheckoutAt;
        }
    }

    private void validateCommonRules(Student student) {
        DomainAssert.notNull(student, AttendanceErrorCode.STUDENT_REQUIRED);
    }
}
