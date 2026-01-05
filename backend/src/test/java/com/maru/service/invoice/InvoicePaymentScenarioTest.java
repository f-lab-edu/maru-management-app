package com.maru.service.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.domain.invoice.*;
import com.maru.domain.student.Student;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("청구서/수납 시나리오 테스트")
class InvoicePaymentScenarioTest {

    private Student student;

    private static final String TENANT_ID = "tenant1";
    private static final String DOJANG_ID = "dojang1";
    private static final String STUDENT_ID = "student1";
    private static final String USER_ID = "user1";
    private static final BigDecimal MONTHLY_FEE = new BigDecimal("100000");

    @BeforeEach
    void setUp() {
        student = createMockStudent();
    }

    @Nested
    @DisplayName("시나리오 1: 기본 수납 완료 흐름")
    class BasicPaymentFlowScenario {

        @Test
        @DisplayName("청구서 생성 → 발행 → 수납 → 완납 확인")
        void fullPaymentFlow() {
            // Given: 청구서 생성
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    "1월 수강료"
            );
            setInvoiceId(invoice, "inv1");

            // Then: 초기 상태 확인
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(invoice.getRemainingAmount()).isEqualByComparingTo(MONTHLY_FEE);

            // When: 발행
            invoice.issue(USER_ID);

            // Then: 발행 상태 확인
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
            assertThat(invoice.getIssueDate()).isNotNull();
            assertThat(invoice.getIssuedBy()).isEqualTo(USER_ID);

            // When: 수납 (전액)
            Payment payment = Payment.create(invoice, MONTHLY_FEE, PaymentMethod.CASH, USER_ID);
            invoice.addPayment(payment.getAmount());

            // Then: 완납 상태 확인
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo(MONTHLY_FEE);
            assertThat(invoice.getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        }
    }

    @Nested
    @DisplayName("시나리오 2: 부분 수납 흐름")
    class PartialPaymentFlowScenario {

        @Test
        @DisplayName("청구서 생성 → 발행 → 부분 수납 → PARTIAL 확인 → 나머지 수납 → 완납")
        void partialPaymentFlow() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);

            BigDecimal firstPayment = new BigDecimal("50000");
            BigDecimal secondPayment = new BigDecimal("50000");

            // When: 첫 번째 부분 수납
            Payment payment1 = Payment.create(invoice, firstPayment, PaymentMethod.CASH, USER_ID);
            invoice.addPayment(payment1.getAmount());

            // Then: PARTIAL 상태 확인
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo(firstPayment);
            assertThat(invoice.getRemainingAmount()).isEqualByComparingTo(secondPayment);

            // When: 두 번째 수납 (나머지 금액)
            Payment payment2 = Payment.create(invoice, secondPayment, PaymentMethod.CARD, USER_ID);
            invoice.addPayment(payment2.getAmount());

            // Then: 완납 상태 확인
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo(MONTHLY_FEE);
            assertThat(invoice.getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("시나리오 3: 수납 취소 흐름")
    class PaymentCancellationScenario {

        @Test
        @DisplayName("청구서 생성 → 발행 → 수납 → 수납 취소 → 상태 복원")
        void paymentCancellationFlow() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);

            // 전액 수납
            Payment payment = Payment.create(invoice, MONTHLY_FEE, PaymentMethod.CASH, USER_ID);
            invoice.addPayment(payment.getAmount());
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);

            // When: 수납 취소 (환불)
            payment.refund();
            invoice.subtractPayment(payment.getAmount());

            // Then: 상태 복원 확인
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(invoice.getRemainingAmount()).isEqualByComparingTo(MONTHLY_FEE);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundedAt()).isNotNull();
        }

        @Test
        @DisplayName("부분 수납 후 취소 → PARTIAL → OPEN 복원")
        void partialPaymentCancellationFlow() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);

            BigDecimal firstPayment = new BigDecimal("30000");
            BigDecimal secondPayment = new BigDecimal("40000");

            // 두 번 수납
            Payment payment1 = Payment.create(invoice, firstPayment, PaymentMethod.CASH, USER_ID);
            invoice.addPayment(payment1.getAmount());

            Payment payment2 = Payment.create(invoice, secondPayment, PaymentMethod.CARD, USER_ID);
            invoice.addPayment(payment2.getAmount());

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);

            // When: 첫 번째 수납 취소
            payment1.refund();
            invoice.subtractPayment(payment1.getAmount());

            // Then: 여전히 PARTIAL (두 번째 수납만 남음)
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo(secondPayment);

            // When: 두 번째 수납도 취소
            payment2.refund();
            invoice.subtractPayment(payment2.getAmount());

            // Then: OPEN으로 복원
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("시나리오 4: 무효화/복구 흐름")
    class VoidRestoreScenario {

        @Test
        @DisplayName("청구서 생성 → 무효화 → 복구 → 삭제 가능 확인")
        void voidRestoreFlow() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );

            // When: 무효화
            invoice.markAsVoid();

            // Then: VOID 상태 확인
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.VOID);

            // When: 복구
            invoice.restore();

            // Then: DRAFT로 복원 및 발행 정보 초기화
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
            assertThat(invoice.getIssueDate()).isNull();
            assertThat(invoice.getIssuedBy()).isNull();
            assertThat(invoice.isEditable()).isTrue();
        }

        @Test
        @DisplayName("발행된 청구서 무효화 → 복구 시 DRAFT로")
        void issuedInvoiceVoidRestoreFlow() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);

            // When: 무효화 후 복구
            invoice.markAsVoid();
            invoice.restore();

            // Then: DRAFT로 복원 (재발행 필요)
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        }
    }

    @Nested
    @DisplayName("시나리오 5: 청구서 수정 흐름")
    class InvoiceUpdateScenario {

        @Test
        @DisplayName("DRAFT 상태에서 수정 가능")
        void updateDraftInvoice() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    "원래 메모"
            );

            BigDecimal newAmount = new BigDecimal("120000");
            LocalDate newDueDate = LocalDate.of(2025, 2, 15);

            // When
            invoice.update(newAmount, newDueDate, "수정된 메모");

            // Then
            assertThat(invoice.getAmount()).isEqualByComparingTo(newAmount);
            assertThat(invoice.getDueDate()).isEqualTo(newDueDate);
            assertThat(invoice.getNote()).isEqualTo("수정된 메모");
        }

        @Test
        @DisplayName("OPEN 상태에서 수정 가능 (수납 없는 발행 상태)")
        void updateOpenInvoice() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);

            BigDecimal newAmount = new BigDecimal("90000");

            // When
            invoice.update(newAmount, null, "할인 적용");

            // Then
            assertThat(invoice.getAmount()).isEqualByComparingTo(newAmount);
            assertThat(invoice.getNote()).isEqualTo("할인 적용");
        }
    }

    @Nested
    @DisplayName("예외 케이스: 잘못된 상태 전이")
    class InvalidStateTransitionScenario {

        @Test
        @DisplayName("OPEN 상태가 아닌 청구서 발행 시도 → 실패")
        void cannotIssueNonDraftInvoice() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);  // 이미 발행됨

            // When & Then
            assertThatThrownBy(() -> invoice.issue(USER_ID))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("DRAFT 상태 청구서에 수납 시도 → 실패")
        void cannotPayDraftInvoice() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );

            // When & Then
            assertThatThrownBy(() -> invoice.addPayment(MONTHLY_FEE))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("VOID 상태 청구서에 수납 시도 → 실패")
        void cannotPayVoidInvoice() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.markAsVoid();

            // When & Then
            assertThatThrownBy(() -> invoice.addPayment(MONTHLY_FEE))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("PAID 상태 청구서 무효화 시도 → 실패")
        void cannotVoidPaidInvoice() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);
            invoice.addPayment(MONTHLY_FEE);
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);

            // When & Then
            assertThatThrownBy(invoice::markAsVoid)
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("VOID가 아닌 청구서 복구 시도 → 실패")
        void cannotRestoreNonVoidInvoice() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );

            // When & Then
            assertThatThrownBy(invoice::restore)
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("PARTIAL/PAID 상태 청구서 수정 시도 → 실패")
        void cannotUpdatePartialOrPaidInvoice() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);
            invoice.addPayment(new BigDecimal("50000"));
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);

            // When & Then
            assertThatThrownBy(() -> invoice.update(new BigDecimal("90000"), null, null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("남은 금액보다 큰 금액 수납 시도 → 실패")
        void cannotOverpay() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    MONTHLY_FEE,
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);

            BigDecimal overAmount = new BigDecimal("150000");

            // When & Then
            assertThatThrownBy(() -> Payment.create(invoice, overAmount, PaymentMethod.CASH, USER_ID))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("시나리오 6: 다양한 결제 방법")
    class PaymentMethodScenario {

        @Test
        @DisplayName("다양한 결제 방법으로 수납")
        void multiplePaymentMethods() {
            // Given
            Invoice invoice = Invoice.create(
                    student, YearMonth.of(2025, 1),
                    new BigDecimal("100000"),
                    LocalDate.of(2025, 1, 31),
                    null
            );
            invoice.issue(USER_ID);

            // When: 여러 결제 방법으로 수납
            Payment cashPayment = Payment.create(invoice, new BigDecimal("30000"), PaymentMethod.CASH, USER_ID);
            invoice.addPayment(cashPayment.getAmount());

            Payment cardPayment = Payment.create(invoice, new BigDecimal("40000"), PaymentMethod.CARD, USER_ID);
            invoice.addPayment(cardPayment.getAmount());

            Payment transferPayment = Payment.create(invoice, new BigDecimal("30000"), PaymentMethod.TRANSFER, USER_ID);
            invoice.addPayment(transferPayment.getAmount());

            // Then
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(cashPayment.getMethod()).isEqualTo(PaymentMethod.CASH);
            assertThat(cardPayment.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(transferPayment.getMethod()).isEqualTo(PaymentMethod.TRANSFER);
        }
    }

    // ========== 헬퍼 메서드 ==========

    private Student createMockStudent() {
        Tenant mockTenant = mock(Tenant.class);
        when(mockTenant.getId()).thenReturn(TENANT_ID);

        Dojang mockDojang = mock(Dojang.class);
        when(mockDojang.getId()).thenReturn(DOJANG_ID);
        when(mockDojang.getTenant()).thenReturn(mockTenant);

        Student mockStudent = mock(Student.class);
        when(mockStudent.getId()).thenReturn(STUDENT_ID);
        when(mockStudent.getTenantId()).thenReturn(TENANT_ID);
        when(mockStudent.getDojang()).thenReturn(mockDojang);
        when(mockStudent.getName()).thenReturn("홍길동");

        return mockStudent;
    }

    private void setInvoiceId(Invoice invoice, String id) {
        ReflectionTestUtils.setField(invoice, "id", id);
    }
}
