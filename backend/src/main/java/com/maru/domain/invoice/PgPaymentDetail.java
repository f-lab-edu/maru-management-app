package com.maru.domain.invoice;

import com.maru.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "pg_payment_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PgPaymentDetail extends BaseEntity {

    @Column(nullable = false, length = 13)
    private String tenantId;

    @Column(nullable = false, length = 13)
    private String dojangId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PgPaymentStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;

    @Column(nullable = false, length = 200, unique = true)
    private String paymentKey;

    @Column(nullable = false, length = 64, unique = true)
    private String orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String pgMethod;

    @Column(length = 500)
    private String receiptUrl;

    @Column(precision = 10, scale = 2)
    private BigDecimal settlementAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal platformFee;

    @Column(columnDefinition = "JSON")
    @ToString.Exclude
    private String responseData;

    private PgPaymentDetail(String tenantId, String dojangId,
                            String paymentKey, String orderId, BigDecimal amount) {
        this.tenantId = tenantId;
        this.dojangId = dojangId;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.status = PgPaymentStatus.PENDING;
    }

    public static PgPaymentDetail createPending(String tenantId, String dojangId,
                                                 String paymentKey, String orderId, BigDecimal amount) {
        return new PgPaymentDetail(tenantId, dojangId, paymentKey, orderId, amount);
    }

    public void confirm(Payment payment, String responseData, String pgMethod,
                        String receiptUrl, BigDecimal settlementAmount, BigDecimal platformFee) {
        this.status = PgPaymentStatus.CONFIRMED;
        this.payment = payment;
        this.responseData = responseData;
        this.pgMethod = pgMethod;
        this.receiptUrl = receiptUrl;
        this.settlementAmount = settlementAmount;
        this.platformFee = platformFee;
    }

    public void markOrphaned() {
        this.status = PgPaymentStatus.ORPHANED;
    }
}
