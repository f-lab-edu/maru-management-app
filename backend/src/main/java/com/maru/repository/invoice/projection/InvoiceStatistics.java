package com.maru.repository.invoice.projection;

import java.math.BigDecimal;

public interface InvoiceStatistics {
    int getPaidCount();
    BigDecimal getTotalUnpaidAmount();
    int getUnpaidCount();
    int getPartialCount();
}
