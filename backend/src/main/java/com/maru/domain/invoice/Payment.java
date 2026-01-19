package com.maru.domain.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.DomainAssert;
import com.maru.domain.invoice.exception.PaymentErrorCode;
import com.maru.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Column(nullable = false, length = 13)
    private String tenantId;

    @Column(nullable = false, length = 13)
    private String dojangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod method;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 13)
    private String receivedBy;

    private Payment(Invoice invoice, BigDecimal amount, PaymentMethod method, String receivedBy) {
        validatePayment(invoice, amount);

        this.tenantId = invoice.getTenantId();
        this.dojangId = invoice.getDojangId();
        this.invoice = invoice;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.receivedBy = receivedBy;
    }

    public static Payment create(Invoice invoice, BigDecimal amount, PaymentMethod method, String receivedBy) {
        return new Payment(invoice, amount, method, receivedBy);
    }

    private void validatePayment(Invoice invoice, BigDecimal amount) {
        DomainAssert.notNull(invoice, PaymentErrorCode.INVOICE_REQUIRED);
        DomainAssert.notNull(amount, PaymentErrorCode.AMOUNT_REQUIRED);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(PaymentErrorCode.INVALID_AMOUNT);
        }

        BigDecimal remainingAmount = invoice.getRemainingAmount();
        if (amount.compareTo(remainingAmount) > 0) {
            throw new BusinessException(PaymentErrorCode.AMOUNT_EXCEEDS_REMAINING);
        }
    }

    public void refund() {
        if (this.status == PaymentStatus.REFUNDED) {
            return;
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }
}
