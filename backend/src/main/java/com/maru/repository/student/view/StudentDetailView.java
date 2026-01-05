package com.maru.repository.student.view;

import com.maru.domain.student.StudentStatus;

import java.time.LocalDate;

public interface StudentDetailView {
    String getId();
    String getName();
    LocalDate getBirth();
    String getPhotoUrl();
    String getPhone();
    LocalDate getEnrolledAt();
    StudentStatus getStatus();
}
