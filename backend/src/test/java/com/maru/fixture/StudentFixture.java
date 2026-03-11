package com.maru.fixture;

import java.time.LocalDate;

import com.maru.domain.student.Student;
import com.maru.support.TenantTestSupport;

public class StudentFixture {

    private String id = "STU_DEFAULT";
    private String tenantId = TenantTestSupport.TEST_TENANT_ID;
    private String dojangId = TenantTestSupport.TEST_DOJANG_ID;
    private String name = "홍길동";
    private LocalDate birth = LocalDate.of(2015, 3, 1);
    private String photoUrl = null;
    private String phone = "01012345678";

    public static StudentFixture aStudent() {
        return new StudentFixture();
    }

    public StudentFixture withId(String id) {
        this.id = id;
        return this;
    }

    public StudentFixture withName(String name) {
        this.name = name;
        return this;
    }

    public Student build() {
        Student student = Student.create(tenantId, dojangId, name, birth, photoUrl, phone);
        FixtureReflectionUtils.setId(student, id);
        return student;
    }
}
