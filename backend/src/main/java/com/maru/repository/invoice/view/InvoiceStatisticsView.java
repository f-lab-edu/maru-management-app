package com.maru.repository.invoice.view;

import java.math.BigDecimal;

public interface InvoiceStatisticsView {
    int getPaidCount();
    int getUnpaidCount();
    int getPartialCount();
    BigDecimal getTotalAmount();
    BigDecimal getTotalPaidAmount();
    BigDecimal getTotalUnpaidAmount();
}
