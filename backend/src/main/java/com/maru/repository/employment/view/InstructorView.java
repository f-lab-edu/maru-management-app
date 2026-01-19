package com.maru.repository.employment.view;

import java.time.LocalDateTime;

public interface InstructorView {
    String getId();
    String getUserId();
    String getUserName();
    String getUserEmail();
    String getUserPhone();
    String getStatus();
    LocalDateTime getJoinedAt();
    LocalDateTime getSuspendedAt();
}
