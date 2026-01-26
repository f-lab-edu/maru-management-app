package com.maru.repository.invoice.view;

import com.maru.domain.invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public interface InvoiceStudentView {
    String getId();
    String getStudentId();
    String getStudentName();
    BigDecimal getAmount();
    BigDecimal getPaidAmount();
    InvoiceStatus getStatus();
    LocalDate getDueDate();
    LocalDate getIssueDate();
    String getNote();
    YearMonth getBillingYearMonth();
    Boolean getStudentDeleted();
}
