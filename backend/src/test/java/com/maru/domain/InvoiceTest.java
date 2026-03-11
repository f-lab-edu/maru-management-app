package com.maru.domain;

import com.maru.common.exception.BusinessException;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.domain.invoice.exception.InvoiceErrorCode;
import com.maru.fixture.InvoiceFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    @Test
    @DisplayName("청구서 생성 시 DRAFT 상태")
    void createInvoice() {
        // given
        // when
        Invoice invoice = InvoiceFixture.anInvoice().build();

        // then
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("청구서 발행 시 OPEN 상태로 전환")
    void issueInvoice() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice().build();

        // when
        invoice.issue("ISSUER_001");

        // then
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
    }

    @Test
    @DisplayName("잔액 계산: 총액 - 납부액")
    void getRemainingAmount() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();

        // when
        invoice.addPayment(new BigDecimal("30000"));

        // then
        assertThat(invoice.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("70000"));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);
    }

    @Test
    @DisplayName("전액 납부 시 PAID 상태")
    void fullPayment() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();

        // when
        invoice.addPayment(new BigDecimal("100000"));

        // then
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("환불 시 납부액 차감 및 상태 복원")
    void subtractPayment() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();
        invoice.addPayment(new BigDecimal("100000"));

        // when
        invoice.subtractPayment(new BigDecimal("50000"));

        // then
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);
        assertThat(invoice.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("VOID 처리")
    void markAsVoid() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice().build();

        // when
        invoice.markAsVoid();

        // then
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.VOID);
    }

    @Test
    @DisplayName("이미 발행된 청구서 재발행 시 예외")
    void issueAlreadyIssuedInvoice() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice().buildIssued();

        // when & then
        assertThatThrownBy(() -> invoice.issue("ISSUER_002"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", InvoiceErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    @DisplayName("VOID가 아닌 청구서 복구 시 예외")
    void restoreNonVoidInvoice() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice().build();

        // when & then
        assertThatThrownBy(() -> invoice.restore())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", InvoiceErrorCode.CANNOT_RESTORE_NON_VOID);
    }

    @Test
    @DisplayName("완납된 청구서 무효화 시 예외")
    void voidPaidInvoice() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();
        invoice.addPayment(new BigDecimal("100000"));

        // when & then
        assertThatThrownBy(() -> invoice.markAsVoid())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", InvoiceErrorCode.CANNOT_VOID_PAID_INVOICE);
    }
}
