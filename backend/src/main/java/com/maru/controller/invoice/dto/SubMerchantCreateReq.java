package com.maru.controller.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SubMerchantCreateReq(
        @NotNull(message = "수수료율은 필수입니다")
        BigDecimal feeRate,

        @NotBlank(message = "은행 코드는 필수입니다")
        @Size(max = 10, message = "은행 코드는 10자 이내여야 합니다")
        String bankCode,

        @NotBlank(message = "계좌번호는 필수입니다")
        @Size(max = 200, message = "계좌번호는 200자 이내여야 합니다")
        String accountNumber,

        @NotBlank(message = "예금주명은 필수입니다")
        @Size(max = 50, message = "예금주명은 50자 이내여야 합니다")
        String accountHolder
) {
}
