package com.maru.fixture;

import java.math.BigDecimal;

import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.PaymentMethod;

public class PaymentFixture {

    private String id = "PAY_DEFAULT";
    private Invoice invoice;
    private BigDecimal amount = new BigDecimal("100000");
    private PaymentMethod method = PaymentMethod.CASH;
    private String receivedBy = "USER_001";

    public static PaymentFixture aPayment() {
        return new PaymentFixture();
    }

    public PaymentFixture withId(String id) {
        this.id = id;
        return this;
    }

    public PaymentFixture withInvoice(Invoice invoice) {
        this.invoice = invoice;
        return this;
    }

    public PaymentFixture withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public PaymentFixture withMethod(PaymentMethod method) {
        this.method = method;
        return this;
    }

    public Payment build() {
        Invoice inv = (invoice != null) ? invoice : InvoiceFixture.anInvoice().buildIssued();
        Payment payment = Payment.create(inv, amount, method, receivedBy);
        FixtureReflectionUtils.setId(payment, id);
        return payment;
    }

    public Payment buildPg() {
        Invoice inv = (invoice != null) ? invoice : InvoiceFixture.anInvoice().buildIssued();
        Payment payment = Payment.createPg(inv, amount);
        FixtureReflectionUtils.setId(payment, id);
        return payment;
    }
}
