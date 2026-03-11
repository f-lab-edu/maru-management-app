package com.maru.service;

import com.maru.common.exception.BusinessException;
import com.maru.controller.invoice.dto.RefundReq;
import com.maru.controller.invoice.dto.RefundRes;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.PgPaymentDetail;
import com.maru.domain.invoice.Refund;
import com.maru.domain.invoice.RefundStatus;
import com.maru.domain.invoice.exception.PaymentErrorCode;
import com.maru.domain.invoice.exception.PgPaymentErrorCode;
import com.maru.domain.invoice.exception.RefundErrorCode;
import com.maru.fixture.InvoiceFixture;
import com.maru.fixture.PaymentFixture;
import com.maru.fixture.PgPaymentDetailFixture;
import com.maru.repository.invoice.PaymentRepository;
import com.maru.repository.invoice.PgPaymentDetailRepository;
import com.maru.repository.invoice.RefundRepository;
import com.maru.service.invoice.RefundService;
import com.maru.service.invoice.TossPaymentClient;
import com.maru.service.invoice.dto.TossCancelRes;
import com.maru.support.ServiceTest;
import com.maru.support.TenantTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ServiceTest
class RefundServiceTest {

    @Mock RefundRepository refundRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PgPaymentDetailRepository pgPaymentDetailRepository;
    @Mock TossPaymentClient tossPaymentClient;
    @Mock ObjectMapper objectMapper;

    @InjectMocks RefundService refundService;

    @BeforeEach
    void setUp() {
        TenantTestSupport.setContext();
    }

    @AfterEach
    void tearDown() {
        TenantTestSupport.clearContext();
    }

    @Test
    @DisplayName("현장 결제 환불 성공")
    void refundOnsite() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();

        Payment payment = PaymentFixture.aPayment()
                .withInvoice(invoice)
                .withAmount(new BigDecimal("100000"))
                .build();
        invoice.addPayment(new BigDecimal("100000"));

        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(payment));

        // when
        RefundRes result = refundService.refund(
                TenantTestSupport.TEST_DOJANG_ID, "PAY_001",
                new RefundReq(new BigDecimal("50000"), "부분 환불"));

        // then
        assertThat(result.status()).isEqualTo(RefundStatus.COMPLETED);
        then(refundRepository).should().save(any(Refund.class));
    }

    @Test
    @DisplayName("PG 결제 환불 - 토스 cancel 성공")
    void refundPg() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();

        Payment payment = PaymentFixture.aPayment()
                .withInvoice(invoice)
                .withAmount(new BigDecimal("100000"))
                .buildPg();
        invoice.addPayment(new BigDecimal("100000"));

        PgPaymentDetail pgDetail = PgPaymentDetailFixture.aPgPaymentDetail().build();

        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(payment));
        given(pgPaymentDetailRepository.findByPaymentId(anyString()))
                .willReturn(Optional.of(pgDetail));
        given(tossPaymentClient.cancel(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .willReturn(new TossCancelRes("tx_key", "환불", new BigDecimal("100000"), "2026-02-19"));

        // when
        RefundRes result = refundService.refund(
                TenantTestSupport.TEST_DOJANG_ID, "PAY_001",
                new RefundReq(new BigDecimal("100000"), "PG 환불"));

        // then
        assertThat(result.status()).isEqualTo(RefundStatus.COMPLETED);
        then(tossPaymentClient).should().cancel(anyString(), anyString(), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("PG 환불 실패 시 paidAmount 복구")
    void refundPgFail_restoresPaidAmount() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();

        Payment payment = PaymentFixture.aPayment()
                .withInvoice(invoice)
                .withAmount(new BigDecimal("100000"))
                .buildPg();
        invoice.addPayment(new BigDecimal("100000"));

        PgPaymentDetail pgDetail = PgPaymentDetailFixture.aPgPaymentDetail().build();

        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(payment));
        given(pgPaymentDetailRepository.findByPaymentId(anyString()))
                .willReturn(Optional.of(pgDetail));
        given(tossPaymentClient.cancel(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .willThrow(new BusinessException(PgPaymentErrorCode.TOSS_CANCEL_FAILED));

        // when & then
        assertThatThrownBy(() -> refundService.refund(
                TenantTestSupport.TEST_DOJANG_ID, "PAY_001",
                new RefundReq(new BigDecimal("100000"), "PG 환불")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PgPaymentErrorCode.TOSS_CANCEL_FAILED);

        assertThat(invoice.getPaidAmount()).isEqualByComparingTo(new BigDecimal("100000"));
    }

    @Test
    @DisplayName("존재하지 않는 결제 환불 시 예외")
    void refundNotFoundPayment() {
        // given
        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refundService.refund(
                TenantTestSupport.TEST_DOJANG_ID, "PAY_NONEXIST",
                new RefundReq(new BigDecimal("50000"), "환불")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("이미 환불된 결제 재환불 시 예외")
    void refundAlreadyRefunded() {
        // given
        Payment payment = PaymentFixture.aPayment().build();
        payment.refund();

        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> refundService.refund(
                TenantTestSupport.TEST_DOJANG_ID, "PAY_001",
                new RefundReq(new BigDecimal("50000"), "재환불")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RefundErrorCode.ALREADY_REFUNDED);
    }

    @Test
    @DisplayName("PG 결제 상세 없이 환불 시 예외")
    void refundPgDetailNotFound() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();

        Payment payment = PaymentFixture.aPayment()
                .withInvoice(invoice)
                .withAmount(new BigDecimal("100000"))
                .buildPg();
        invoice.addPayment(new BigDecimal("100000"));

        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(payment));
        given(pgPaymentDetailRepository.findByPaymentId(anyString()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refundService.refund(
                TenantTestSupport.TEST_DOJANG_ID, "PAY_001",
                new RefundReq(new BigDecimal("100000"), "PG 환불")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PgPaymentErrorCode.PG_DETAIL_NOT_FOUND);
    }

    @Test
    @DisplayName("현장 결제 전액 환불 성공")
    void refundFullOnsite() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();

        Payment payment = PaymentFixture.aPayment()
                .withInvoice(invoice)
                .withAmount(new BigDecimal("100000"))
                .build();
        invoice.addPayment(new BigDecimal("100000"));

        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(payment));

        // when
        refundService.refundFull(TenantTestSupport.TEST_DOJANG_ID, "PAY_001");

        // then
        then(refundRepository).should().save(any(Refund.class));
    }

    @Test
    @DisplayName("PG 결제 전액 환불 성공")
    void refundFullPg() {
        // given
        Invoice invoice = InvoiceFixture.anInvoice()
                .withAmount(new BigDecimal("100000"))
                .buildIssued();

        Payment payment = PaymentFixture.aPayment()
                .withInvoice(invoice)
                .withAmount(new BigDecimal("100000"))
                .buildPg();
        invoice.addPayment(new BigDecimal("100000"));

        PgPaymentDetail pgDetail = PgPaymentDetailFixture.aPgPaymentDetail().build();

        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(payment));
        given(pgPaymentDetailRepository.findByPaymentId(anyString()))
                .willReturn(Optional.of(pgDetail));
        given(tossPaymentClient.cancel(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .willReturn(new TossCancelRes("tx_key", "전액 환불", new BigDecimal("100000"), "2026-02-19"));

        // when
        refundService.refundFull(TenantTestSupport.TEST_DOJANG_ID, "PAY_001");

        // then
        then(tossPaymentClient).should().cancel(anyString(), anyString(), any(BigDecimal.class), anyString());
        then(refundRepository).should().save(any(Refund.class));
    }

    @Test
    @DisplayName("이미 환불된 결제 전액 환불 시 예외")
    void refundFullAlreadyRefunded() {
        // given
        Payment payment = PaymentFixture.aPayment().build();
        payment.refund();

        given(paymentRepository.findByIdAndTenantIdAndDojangId(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> refundService.refundFull(
                TenantTestSupport.TEST_DOJANG_ID, "PAY_001"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RefundErrorCode.ALREADY_REFUNDED);
    }
}
