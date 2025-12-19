package com.maru.domain.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.DomainAssert;
import com.maru.common.exception.PaymentErrorCode;
import com.maru.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "payment",
    indexes = {
        @Index(name = "idx_payment_tenant_paid_at", columnList = "tenant_id, paid_at"),
        @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
        @Index(name = "idx_payment_status", columnList = "status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "dojang_id", nullable = false)
    private Long dojangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 20)
    private PaymentMethod method;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "received_by")
    private Long receivedBy;

    @Column(name = "note", length = 500)
    private String note;

    private Payment(Invoice invoice, BigDecimal amount, PaymentMethod method, Long receivedBy, String note) {
        validatePayment(invoice, amount);

        this.tenantId = invoice.getTenantId();
        this.dojangId = invoice.getDojangId();
        this.invoice = invoice;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.receivedBy = receivedBy;
        this.note = note;
    }

    public static Payment create(Invoice invoice, BigDecimal amount, PaymentMethod method, Long receivedBy, String note) {
        return new Payment(invoice, amount, method, receivedBy, note);
    }

    private void validatePayment(Invoice invoice, BigDecimal amount) {
        DomainAssert.notNull(invoice, PaymentErrorCode.INVALID_AMOUNT);
        DomainAssert.notNull(amount, PaymentErrorCode.INVALID_AMOUNT);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(PaymentErrorCode.INVALID_AMOUNT);
        }

        BigDecimal remainingAmount = invoice.getRemainingAmount();
        if (amount.compareTo(remainingAmount) > 0) {
            throw new BusinessException(PaymentErrorCode.AMOUNT_EXCEEDS_REMAINING);
        }
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }
}
