package com.maru.repository.invoice.view;

import java.math.BigDecimal;

public interface InvoiceStatisticsView {
    int getPaidCount();
    BigDecimal getTotalUnpaidAmount();
    int getUnpaidCount();
    int getPartialCount();
}
