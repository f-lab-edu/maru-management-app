package com.maru.repository.student.view;

public interface StudentCountView {
    long getTotalCount();
    long getActiveCount();
    long getPausedCount();
}
