package com.maru.repository.enrollment.view;

import java.time.LocalDateTime;

public interface EnrollmentStudentView {
    String getStudentId();
    String getStudentName();
    LocalDateTime getCreatedAt();
}
