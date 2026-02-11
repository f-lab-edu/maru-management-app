package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.SubMerchant;
import com.maru.domain.invoice.SubMerchantStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SubMerchantRes(
        String id,
        String dojangId,
        SubMerchantStatus status,
        BigDecimal feeRate,
        String bankCode,
        String maskedAccountNumber,
        String accountHolder
) {
    public static SubMerchantRes from(SubMerchant subMerchant) {
        return SubMerchantRes.builder()
                .id(subMerchant.getId())
                .dojangId(subMerchant.getDojangId())
                .status(subMerchant.getStatus())
                .feeRate(subMerchant.getFeeRate())
                .bankCode(subMerchant.getBankCode())
                .maskedAccountNumber(maskAccountNumber(subMerchant.getAccountNumberEnc()))
                .accountHolder(subMerchant.getAccountHolder())
                .build();
    }

    private static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        return "*".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
    }
}
