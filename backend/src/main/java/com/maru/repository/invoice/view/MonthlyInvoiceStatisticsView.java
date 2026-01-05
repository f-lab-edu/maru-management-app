package com.maru.repository.invoice.view;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface MonthlyInvoiceStatisticsView {
    YearMonth getYearMonth();
    BigDecimal getTotalAmount();
    BigDecimal getTotalPaidAmount();
    BigDecimal getTotalUnpaidAmount();
    int getPaidCount();
    int getPartialCount();
    int getUnpaidCount();
}
