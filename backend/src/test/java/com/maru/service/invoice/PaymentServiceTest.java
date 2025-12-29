package com.maru.service.invoice;

import com.maru.controller.invoice.dto.*;
import com.maru.domain.invoice.*;
import com.maru.domain.student.Student;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.PaymentRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private DojangRepository dojangRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GuardianshipRepository guardianshipRepository;

    @InjectMocks
    private PaymentService paymentService;

    private static final Long TENANT_ID = 1L;
    private static final Long DOJANG_ID = 1L;
    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("수납 기록")
    class RecordPaymentTest {

        @Test
        @DisplayName("전액 수납 시 PAID 상태로 변경")
        void recordPayment_fullAmount_becomesPaid() {
            try (MockedStatic<TenantContextHolder> mockedStatic = mockStatic(TenantContextHolder.class)) {
                // Given
                mockedStatic.when(TenantContextHolder::getTenantId).thenReturn(TENANT_ID);

                Dojang mockDojang = createMockDojang();
                BigDecimal amount = new BigDecimal("100000");
                Invoice invoice = createInvoice(1L, InvoiceStatus.OPEN, amount, mockDojang);

                given(dojangRepository.findById(DOJANG_ID)).willReturn(Optional.of(mockDojang));
                given(invoiceRepository.findByIdAndDojangIdWithStudent(TENANT_ID, DOJANG_ID, 1L))
                        .willReturn(Optional.of(invoice));
                given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    ReflectionTestUtils.setField(payment, "id", 1L);
                    return payment;
                });
                given(paymentRepository.findByInvoiceIdOrderByPaidAtDesc(1L))
                        .willReturn(List.of());

                PaymentRecordReq request = PaymentRecordReq.builder()
                        .amount(amount)
                        .method(PaymentMethod.CASH)
                        .build();

                // When
                InvoiceDetailRes result = paymentService.recordPayment(DOJANG_ID, 1L, request, USER_ID);

                // Then
                assertThat(result.status()).isEqualTo(InvoiceStatus.PAID);
                assertThat(result.paidAmount()).isEqualByComparingTo(amount);
                assertThat(result.remainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            }
        }

        @Test
        @DisplayName("부분 수납 시 PARTIAL 상태로 변경")
        void recordPayment_partialAmount_becomesPartial() {
            try (MockedStatic<TenantContextHolder> mockedStatic = mockStatic(TenantContextHolder.class)) {
                // Given
                mockedStatic.when(TenantContextHolder::getTenantId).thenReturn(TENANT_ID);

                Dojang mockDojang = createMockDojang();
                BigDecimal amount = new BigDecimal("100000");
                BigDecimal partialAmount = new BigDecimal("50000");
                Invoice invoice = createInvoice(1L, InvoiceStatus.OPEN, amount, mockDojang);

                given(dojangRepository.findById(DOJANG_ID)).willReturn(Optional.of(mockDojang));
                given(invoiceRepository.findByIdAndDojangIdWithStudent(TENANT_ID, DOJANG_ID, 1L))
                        .willReturn(Optional.of(invoice));
                given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    ReflectionTestUtils.setField(payment, "id", 1L);
                    return payment;
                });
                given(paymentRepository.findByInvoiceIdOrderByPaidAtDesc(1L))
                        .willReturn(List.of());

                PaymentRecordReq request = PaymentRecordReq.builder()
                        .amount(partialAmount)
                        .method(PaymentMethod.CARD)
                        .build();

                // When
                InvoiceDetailRes result = paymentService.recordPayment(DOJANG_ID, 1L, request, USER_ID);

                // Then
                assertThat(result.status()).isEqualTo(InvoiceStatus.PARTIAL);
                assertThat(result.paidAmount()).isEqualByComparingTo(partialAmount);
                assertThat(result.remainingAmount()).isEqualByComparingTo(partialAmount);
            }
        }
    }

    @Nested
    @DisplayName("수납 취소")
    class CancelPaymentTest {

        @Test
        @DisplayName("수납 취소 시 상태 복원")
        void cancelPayment_restoresStatus() {
            try (MockedStatic<TenantContextHolder> mockedStatic = mockStatic(TenantContextHolder.class)) {
                // Given
                mockedStatic.when(TenantContextHolder::getTenantId).thenReturn(TENANT_ID);

                Dojang mockDojang = createMockDojang();
                BigDecimal amount = new BigDecimal("100000");

                InvoiceWithPayment invoiceWithPayment = createPaidInvoiceWithPayment(
                        1L, amount, mockDojang, 1L
                );
                Invoice invoice = invoiceWithPayment.invoice();
                Payment payment = invoiceWithPayment.payment();

                given(dojangRepository.findById(DOJANG_ID)).willReturn(Optional.of(mockDojang));
                given(invoiceRepository.findByIdAndDojangIdWithStudent(TENANT_ID, DOJANG_ID, 1L))
                        .willReturn(Optional.of(invoice));
                given(paymentRepository.findByIdAndTenantIdAndDojangId(1L, TENANT_ID, DOJANG_ID))
                        .willReturn(Optional.of(payment));
                given(paymentRepository.findByInvoiceIdOrderByPaidAtDesc(1L))
                        .willReturn(List.of());

                // When
                InvoiceDetailRes result = paymentService.cancelPayment(DOJANG_ID, 1L, 1L, USER_ID);

                // Then
                assertThat(result.status()).isEqualTo(InvoiceStatus.OPEN);
                assertThat(result.paidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
                assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            }
        }
    }

    // ========== 헬퍼 메서드 ==========

    private Dojang createMockDojang() {
        Tenant mockTenant = mock(Tenant.class);
        lenient().when(mockTenant.getId()).thenReturn(TENANT_ID);

        Dojang mockDojang = mock(Dojang.class);
        lenient().when(mockDojang.getId()).thenReturn(DOJANG_ID);
        lenient().when(mockDojang.getTenant()).thenReturn(mockTenant);

        return mockDojang;
    }

    private Student createMockStudent(Long id, String name, Dojang dojang) {
        Student mockStudent = mock(Student.class);
        lenient().when(mockStudent.getId()).thenReturn(id);
        lenient().when(mockStudent.getTenantId()).thenReturn(TENANT_ID);
        lenient().when(mockStudent.getDojang()).thenReturn(dojang);
        lenient().when(mockStudent.getName()).thenReturn(name);

        return mockStudent;
    }

    private Invoice createInvoice(Long id, InvoiceStatus status, BigDecimal amount, Dojang dojang) {
        Student student = createMockStudent(100L, "테스트학생", dojang);

        Invoice invoice = Invoice.create(
                student, 2025, 1,
                amount,
                LocalDate.of(2025, 1, 31),
                null
        );

        switch (status) {
            case DRAFT -> {
            }
            case OPEN -> {
                invoice.issue(USER_ID);
            }
            case PARTIAL -> {
                invoice.issue(USER_ID);
                invoice.addPayment(amount.divide(new BigDecimal("2")));
            }
            case PAID -> {
                invoice.issue(USER_ID);
                invoice.addPayment(amount);
            }
            case VOID -> {
                invoice.markAsVoid();
            }
        }

        // id만 Reflection (Mock Repository 환경에서 불가피)
        ReflectionTestUtils.setField(invoice, "id", id);

        return invoice;
    }

    private InvoiceWithPayment createPaidInvoiceWithPayment(
            Long invoiceId,
            BigDecimal amount,
            Dojang dojang,
            Long paymentId
    ) {
        Student student = createMockStudent(100L, "테스트학생", dojang);

        Invoice invoice = Invoice.create(
                student, 2025, 1,
                amount,
                LocalDate.of(2025, 1, 31),
                null
        );
        ReflectionTestUtils.setField(invoice, "id", invoiceId);

        invoice.issue(USER_ID);

        Payment payment = Payment.create(invoice, amount, PaymentMethod.CASH, USER_ID);
        ReflectionTestUtils.setField(payment, "id", paymentId);

        invoice.addPayment(amount);

        return new InvoiceWithPayment(invoice, payment);
    }

    private record InvoiceWithPayment(Invoice invoice, Payment payment) {}
}
