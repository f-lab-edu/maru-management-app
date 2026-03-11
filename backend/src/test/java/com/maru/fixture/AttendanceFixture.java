package com.maru.fixture;

import java.time.LocalDateTime;

import com.maru.domain.attendance.Attendance;
import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;
import com.maru.support.TenantTestSupport;

public class AttendanceFixture {

    private String id = "ATT_DEFAULT";
    private String tenantId = TenantTestSupport.TEST_TENANT_ID;
    private String dojangId = TenantTestSupport.TEST_DOJANG_ID;
    private String studentId = "STU_DEFAULT";
    private CheckMethod method = CheckMethod.MANUAL;
    private AttendanceStatus status = AttendanceStatus.PRESENT;
    private LocalDateTime checkinAt = LocalDateTime.of(2026, 2, 19, 15, 0);
    private String note = null;

    public static AttendanceFixture anAttendance() {
        return new AttendanceFixture();
    }

    public AttendanceFixture withId(String id) {
        this.id = id;
        return this;
    }

    public AttendanceFixture withStudentId(String studentId) {
        this.studentId = studentId;
        return this;
    }

    public AttendanceFixture withStatus(AttendanceStatus status) {
        this.status = status;
        return this;
    }

    public AttendanceFixture withCheckinAt(LocalDateTime checkinAt) {
        this.checkinAt = checkinAt;
        return this;
    }

    public Attendance build() {
        Attendance attendance = Attendance.create(tenantId, dojangId, studentId, method, status, checkinAt, note);
        FixtureReflectionUtils.setId(attendance, id);
        return attendance;
    }
}
