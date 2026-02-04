package com.maru.repository.tenant.view;

/**
 * 자동 청구서 발행 대상 도장 Projection
 */
public interface AutoInvoiceTargetView {
    String getDojangId();
    String getTenantId();
    Integer getDefaultTuition();
}
