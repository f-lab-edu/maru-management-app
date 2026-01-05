package com.maru.repository.student.view;

import com.maru.domain.student.StudentStatus;

import java.time.LocalDate;

public interface StudentSummaryView {
    String getId();
    String getName();
    LocalDate getBirth();
    String getPhotoUrl();
    LocalDate getEnrolledAt();
    StudentStatus getStatus();
    Boolean getHasEnrollment();
}
