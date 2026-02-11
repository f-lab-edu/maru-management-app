package com.maru.domain.invoice;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.invoice.exception.RefundErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refund")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Column(nullable = false, length = 13)
    private String tenantId;

    @Column(nullable = false, length = 13)
    private String dojangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, length = 64, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 200)
    private String reason;

    @Column(length = 200)
    private String pgRefundKey;

    @Column(columnDefinition = "JSON")
    @ToString.Exclude
    private String responseData;

    private LocalDateTime completedAt;

    private Refund(Payment payment, BigDecimal amount, String reason,
                   RefundStatus status, String idempotencyKey) {
        this.tenantId = payment.getTenantId();
        this.dojangId = payment.getDojangId();
        this.payment = payment;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        if (status == RefundStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public static Refund createOnsite(Payment payment, BigDecimal amount, String reason) {
        validateAmount(payment, amount);
        return new Refund(payment, amount, reason, RefundStatus.COMPLETED, UUID.randomUUID().toString());
    }

    public static Refund createPg(Payment payment, BigDecimal amount, String reason) {
        validateAmount(payment, amount);
        return new Refund(payment, amount, reason, RefundStatus.PENDING, UUID.randomUUID().toString());
    }

    public void complete(String pgRefundKey, String responseData) {
        this.status = RefundStatus.COMPLETED;
        this.pgRefundKey = pgRefundKey;
        this.responseData = responseData;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = RefundStatus.FAILED;
    }

    private static void validateAmount(Payment payment, BigDecimal amount) {
        DomainAssert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0,
                RefundErrorCode.AMOUNT_MUST_BE_POSITIVE);
        DomainAssert.isTrue(amount.compareTo(payment.getAmount()) <= 0,
                RefundErrorCode.AMOUNT_EXCEEDS_PAYMENT);
    }
}
