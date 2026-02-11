package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.SubMerchantStatus;
import jakarta.validation.constraints.NotNull;

public record SubMerchantStatusUpdateReq(
        @NotNull(message = "변경할 상태는 필수입니다")
        SubMerchantStatus status
) {
}
