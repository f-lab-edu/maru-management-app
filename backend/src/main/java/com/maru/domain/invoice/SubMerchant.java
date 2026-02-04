package com.maru.domain.invoice;

import com.maru.domain.common.BaseEntity;
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
}
