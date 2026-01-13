package com.maru.repository.student.view;

import java.time.LocalDate;

public interface RecentStudentView {
    String getId();
    String getName();
    LocalDate getEnrolledAt();
    String getStatus();
    String getPhotoUrl();
}
