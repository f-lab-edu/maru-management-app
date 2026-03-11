package com.maru.fixture;

import java.math.BigDecimal;

import com.maru.domain.invoice.PgPaymentDetail;
import com.maru.support.TenantTestSupport;

public class PgPaymentDetailFixture {

    private String id = "PGD_DEFAULT";
    private String tenantId = TenantTestSupport.TEST_TENANT_ID;
    private String dojangId = TenantTestSupport.TEST_DOJANG_ID;
    private String paymentKey = "toss_pk_test_001";
    private String orderId = "ORDER_TEST_001";
    private BigDecimal amount = new BigDecimal("100000");

    public static PgPaymentDetailFixture aPgPaymentDetail() {
        return new PgPaymentDetailFixture();
    }

    public PgPaymentDetailFixture withId(String id) {
        this.id = id;
        return this;
    }

    public PgPaymentDetailFixture withPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
        return this;
    }

    public PgPaymentDetailFixture withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public PgPaymentDetail build() {
        PgPaymentDetail detail = PgPaymentDetail.createPending(tenantId, dojangId, paymentKey, orderId, amount);
        FixtureReflectionUtils.setId(detail, id);
        return detail;
    }
}
