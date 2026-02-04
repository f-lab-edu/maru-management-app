package com.maru.domain.invoice;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseTimeEntity;
import com.maru.domain.invoice.exception.PgPaymentErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLink extends BaseTimeEntity {

    private static final long DEFAULT_EXPIRY_HOURS = 24;

    @Column(nullable = false, length = 13)
    private String tenantId;

    @Column(nullable = false, length = 13)
    private String dojangId;

    @Column(nullable = false, length = 13)
    private String invoiceId;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private PaymentLink(String tenantId, String dojangId, String invoiceId,
                        String token, LocalDateTime expiresAt) {
        this.tenantId = tenantId;
        this.dojangId = dojangId;
        this.invoiceId = invoiceId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static PaymentLink create(String tenantId, String dojangId, String invoiceId) {
        return new PaymentLink(tenantId, dojangId, invoiceId,
                UUID.randomUUID().toString(), LocalDateTime.now().plusHours(DEFAULT_EXPIRY_HOURS));
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isUsed() {
        return this.usedAt != null;
    }

    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }

    public void validateUsable() {
        DomainAssert.isFalse(isExpired(), PgPaymentErrorCode.PAYMENT_LINK_EXPIRED);
        DomainAssert.isFalse(isUsed(), PgPaymentErrorCode.PAYMENT_LINK_ALREADY_USED);
    }
}
