package com.maru.repository.invoice.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public interface UnpaidInvoiceView {
    String getInvoiceId();
    String getStudentId();
    String getStudentName();
    BigDecimal getAmount();
    BigDecimal getPaidAmount();
    LocalDate getDueDate();
    YearMonth getBillingYearMonth();
}
