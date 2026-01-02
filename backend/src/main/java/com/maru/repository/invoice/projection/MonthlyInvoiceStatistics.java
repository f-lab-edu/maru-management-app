package com.maru.repository.invoice.projection;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface MonthlyInvoiceStatistics {
    YearMonth getYearMonth();
    BigDecimal getTotalAmount();
    BigDecimal getTotalPaidAmount();
    BigDecimal getTotalUnpaidAmount();
    int getPaidCount();
    int getPartialCount();
    int getUnpaidCount();
}
