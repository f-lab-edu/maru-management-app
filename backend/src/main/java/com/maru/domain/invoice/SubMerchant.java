package com.maru.domain.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.invoice.exception.SubMerchantErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "sub_merchant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubMerchant extends BaseEntity {

    @Column(nullable = false, length = 13)
    private String tenantId;

    @Column(nullable = false, length = 13, unique = true)
    private String dojangId;

    @Column(length = 100, unique = true)
    private String tossSellerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubMerchantStatus status;

    @Column(precision = 5, scale = 4)
    private BigDecimal feeRate;

    @Column(length = 10)
    private String bankCode;

    @Column(length = 200)
    @ToString.Exclude
    private String accountNumberEnc;

    @Column(length = 50)
    private String accountHolder;

    private SubMerchant(String tenantId, String dojangId, BigDecimal feeRate,
                        String bankCode, String accountNumberEnc, String accountHolder) {
        this.tenantId = tenantId;
        this.dojangId = dojangId;
        this.status = SubMerchantStatus.PENDING;
        this.feeRate = feeRate;
        this.bankCode = bankCode;
        this.accountNumberEnc = accountNumberEnc;
        this.accountHolder = accountHolder;
    }

    public static SubMerchant create(String tenantId, String dojangId, BigDecimal feeRate,
                                     String bankCode, String accountNumber, String accountHolder) {
        return new SubMerchant(tenantId, dojangId, feeRate, bankCode, accountNumber, accountHolder);
    }

    public void activate(String tossSellerId) {
        validateStatusTransition(SubMerchantStatus.ACTIVE);
        this.status = SubMerchantStatus.ACTIVE;
        this.tossSellerId = tossSellerId;
    }

    public void reject() {
        validateStatusTransition(SubMerchantStatus.REJECTED);
        this.status = SubMerchantStatus.REJECTED;
    }

    public void suspend() {
        validateStatusTransition(SubMerchantStatus.SUSPENDED);
        this.status = SubMerchantStatus.SUSPENDED;
    }

    private void validateStatusTransition(SubMerchantStatus targetStatus) {
        boolean valid = switch (targetStatus) {
            case ACTIVE -> this.status == SubMerchantStatus.PENDING;
            case REJECTED -> this.status == SubMerchantStatus.PENDING;
            case SUSPENDED -> this.status == SubMerchantStatus.ACTIVE;
            case PENDING -> false;
        };

        if (!valid) {
            throw new BusinessException(SubMerchantErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}
