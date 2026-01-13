package com.maru.repository.employment.view;

import java.time.LocalDateTime;

public interface RecentApprovalView {
    String getUserId();
    String getUserName();
    LocalDateTime getApprovedAt();
}
