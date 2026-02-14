package com.maru.repository.message.view;

import java.time.LocalDate;

public interface NotificationSummaryView {
    LocalDate getSendDate();
    String getMessageType();
    long getTotalCount();
    long getAcceptedCount();
    long getFailedCount();
}
