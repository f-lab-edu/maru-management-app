package com.maru.fixture;

import com.maru.domain.invoice.PaymentLink;
import com.maru.support.TenantTestSupport;

public class PaymentLinkFixture {

    private String id = "PL_DEFAULT";
    private String tenantId = TenantTestSupport.TEST_TENANT_ID;
    private String dojangId = TenantTestSupport.TEST_DOJANG_ID;
    private String invoiceId = "INV_DEFAULT";

    public static PaymentLinkFixture aPaymentLink() {
        return new PaymentLinkFixture();
    }

    public PaymentLinkFixture withId(String id) {
        this.id = id;
        return this;
    }

    public PaymentLinkFixture withInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }

    public PaymentLink build() {
        PaymentLink link = PaymentLink.create(tenantId, dojangId, invoiceId);
        FixtureReflectionUtils.setId(link, id);
        return link;
    }
}
