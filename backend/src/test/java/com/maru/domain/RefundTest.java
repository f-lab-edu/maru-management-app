package com.maru.domain;

import com.maru.common.exception.BusinessException;
import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.Refund;
import com.maru.domain.invoice.RefundStatus;
import com.maru.domain.invoice.exception.RefundErrorCode;
import com.maru.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundTest {

    @Test
    @DisplayName("현장 환불 생성 시 COMPLETED 상태")
    void createOnsiteRefund() {
        // given
        Payment payment = PaymentFixture.aPayment()
                .withAmount(new BigDecimal("100000"))
                .build();

        // when
        Refund refund = Refund.createOnsite(payment, new BigDecimal("50000"), "현장 환불");

        // then
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
        assertThat(refund.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("PG 환불 생성 시 PENDING 상태")
    void createPgRefund() {
        // given
        Payment payment = PaymentFixture.aPayment()
                .withAmount(new BigDecimal("100000"))
                .buildPg();

        // when
        Refund refund = Refund.createPg(payment, new BigDecimal("100000"), "PG 환불");

        // then
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.PENDING);
    }

    @Test
    @DisplayName("환불 금액이 결제 금액 초과 시 예외")
    void refundAmountExceedsPayment() {
        // given
        Payment payment = PaymentFixture.aPayment()
                .withAmount(new BigDecimal("50000"))
                .build();

        // when & then
        assertThatThrownBy(() -> Refund.createOnsite(payment, new BigDecimal("100000"), "초과 환불"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RefundErrorCode.AMOUNT_EXCEEDS_PAYMENT);
    }

    @Test
    @DisplayName("환불 금액 0 이하 시 예외")
    void refundZeroAmount() {
        // given
        Payment payment = PaymentFixture.aPayment().build();

        // when & then
        assertThatThrownBy(() -> Refund.createOnsite(payment, BigDecimal.ZERO, "0원 환불"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RefundErrorCode.AMOUNT_MUST_BE_POSITIVE);
    }

    @Test
    @DisplayName("PG 환불 완료 처리")
    void completePgRefund() {
        // given
        Payment payment = PaymentFixture.aPayment()
                .withAmount(new BigDecimal("100000"))
                .buildPg();
        Refund refund = Refund.createPg(payment, new BigDecimal("100000"), "PG 환불");

        // when
        refund.complete("pg_refund_key_001", "{\"status\":\"DONE\"}");

        // then
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
        assertThat(refund.getPgRefundKey()).isEqualTo("pg_refund_key_001");
        assertThat(refund.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("PG 환불 실패 처리")
    void failPgRefund() {
        // given
        Payment payment = PaymentFixture.aPayment()
                .withAmount(new BigDecimal("100000"))
                .buildPg();
        Refund refund = Refund.createPg(payment, new BigDecimal("100000"), "PG 환불");

        // when
        refund.fail();

        // then
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.FAILED);
    }
}
