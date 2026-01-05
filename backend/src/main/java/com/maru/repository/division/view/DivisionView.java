package com.maru.repository.division.view;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public interface DivisionView {
    String getId();
    String getName();
    Integer getDisplayOrder();
    Set<DayOfWeek> getScheduleDays();
    LocalTime getStartTime();
    LocalTime getEndTime();
    String getSectionId();
    String getSectionName();
    Integer getStudentCount();
}
