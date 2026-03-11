package com.maru.fixture;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import com.maru.domain.invoice.Invoice;
import com.maru.support.TenantTestSupport;

public class InvoiceFixture {

    private String id = "INV_DEFAULT";
    private String tenantId = TenantTestSupport.TEST_TENANT_ID;
    private String dojangId = TenantTestSupport.TEST_DOJANG_ID;
    private String studentId = "STU_DEFAULT";
    private YearMonth billingYearMonth = YearMonth.of(2026, 2);
    private BigDecimal amount = new BigDecimal("100000");
    private LocalDate dueDate = LocalDate.of(2026, 2, 28);
    private String note = null;

    public static InvoiceFixture anInvoice() {
        return new InvoiceFixture();
    }

    public InvoiceFixture withId(String id) {
        this.id = id;
        return this;
    }

    public InvoiceFixture withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public InvoiceFixture withDojangId(String dojangId) {
        this.dojangId = dojangId;
        return this;
    }

    public InvoiceFixture withStudentId(String studentId) {
        this.studentId = studentId;
        return this;
    }

    public InvoiceFixture withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public InvoiceFixture withBillingYearMonth(YearMonth billingYearMonth) {
        this.billingYearMonth = billingYearMonth;
        return this;
    }

    public InvoiceFixture withDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public Invoice build() {
        Invoice invoice = Invoice.create(tenantId, dojangId, studentId, billingYearMonth, amount, dueDate, note);
        FixtureReflectionUtils.setId(invoice, id);
        return invoice;
    }

    public Invoice buildIssued() {
        Invoice invoice = build();
        invoice.issue("ISSUER_001");
        return invoice;
    }
}
