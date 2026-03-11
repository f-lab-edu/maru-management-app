package com.maru.domain;

import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.PgPaymentDetail;
import com.maru.domain.invoice.PgPaymentStatus;
import com.maru.fixture.PaymentFixture;
import com.maru.fixture.PgPaymentDetailFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PgPaymentDetailTest {

    @Test
    @DisplayName("PENDING 상태로 생성")
    void createPending() {
        // given & when
        PgPaymentDetail detail = PgPaymentDetailFixture.aPgPaymentDetail().build();

        // then
        assertThat(detail.getStatus()).isEqualTo(PgPaymentStatus.PENDING);
    }

    @Test
    @DisplayName("승인 완료 시 CONFIRMED 상태 + Payment 연결")
    void confirm() {
        // given
        PgPaymentDetail detail = PgPaymentDetailFixture.aPgPaymentDetail().build();
        Payment payment = PaymentFixture.aPayment().buildPg();

        // when
        detail.confirm(payment, "{}", "카드", "https://receipt.url",
                new BigDecimal("97000"), new BigDecimal("3000"));

        // then
        assertThat(detail.getStatus()).isEqualTo(PgPaymentStatus.CONFIRMED);
        assertThat(detail.getPayment()).isEqualTo(payment);
        assertThat(detail.getPgMethod()).isEqualTo("카드");
    }

    @Test
    @DisplayName("ORPHANED 마킹")
    void markOrphaned() {
        // given
        PgPaymentDetail detail = PgPaymentDetailFixture.aPgPaymentDetail().build();

        // when
        detail.markOrphaned();

        // then
        assertThat(detail.getStatus()).isEqualTo(PgPaymentStatus.ORPHANED);
    }
}
