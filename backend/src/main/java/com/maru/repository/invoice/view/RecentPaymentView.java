package com.maru.repository.invoice.view;

import java.time.LocalDateTime;

public interface RecentPaymentView {
    String getStudentId();
    String getStudentName();
    LocalDateTime getPaidAt();
}
