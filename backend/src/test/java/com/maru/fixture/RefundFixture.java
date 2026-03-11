package com.maru.fixture;

import java.math.BigDecimal;

import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.Refund;

public class RefundFixture {

    private Payment payment;
    private BigDecimal amount = new BigDecimal("50000");
    private String reason = "테스트 환불 사유";

    public static RefundFixture aRefund() {
        return new RefundFixture();
    }

    public RefundFixture withPayment(Payment payment) {
        this.payment = payment;
        return this;
    }

    public RefundFixture withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public Refund buildOnsite() {
        Payment pay = (payment != null) ? payment : PaymentFixture.aPayment().build();
        return Refund.createOnsite(pay, amount, reason);
    }

    public Refund buildPg() {
        Payment pay = (payment != null) ? payment : PaymentFixture.aPayment().buildPg();
        return Refund.createPg(pay, amount, reason);
    }
}
