package com.maru.service.invoice;

import com.maru.controller.invoice.dto.*;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.domain.student.Student;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("InvoiceService 테스트")
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DojangRepository dojangRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private static final Long TENANT_ID = 1L;
    private static final Long DOJANG_ID = 1L;
    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("일괄 청구서 발행")
    class BulkIssueInvoicesTest {

        @Test
        @DisplayName("DRAFT 상태만 발행 성공, 나머지는 실패 목록에 추가")
        void bulkIssue_onlyDraftSucceeds() {
            try (MockedStatic<TenantContextHolder> mockedStatic = mockStatic(TenantContextHolder.class)) {
                // Given
                mockedStatic.when(TenantContextHolder::getTenantId).thenReturn(TENANT_ID);

                Dojang mockDojang = createMockDojang();
                Invoice draftInvoice1 = createInvoice(1L, InvoiceStatus.DRAFT, mockDojang);
                Invoice draftInvoice2 = createInvoice(2L, InvoiceStatus.DRAFT, mockDojang);
                Invoice openInvoice = createInvoice(3L, InvoiceStatus.OPEN, mockDojang);  // 이미 발행됨

                given(dojangRepository.findById(DOJANG_ID)).willReturn(Optional.of(mockDojang));
                given(invoiceRepository.findAllByDojangIdAndIdIn(TENANT_ID, DOJANG_ID, List.of(1L, 2L, 3L)))
                        .willReturn(List.of(draftInvoice1, draftInvoice2, openInvoice));

                BulkIssueReq request = BulkIssueReq.builder()
                        .invoiceIds(List.of(1L, 2L, 3L))
                        .build();

                // When
                BulkIssueRes result = invoiceService.bulkIssueInvoices(DOJANG_ID, request, USER_ID);

                // Then
                assertThat(result.issuedCount()).isEqualTo(2);
                assertThat(result.failedCount()).isEqualTo(1);
                assertThat(result.failedInvoices()).hasSize(1);
                assertThat(result.failedInvoices().getFirst().invoiceId()).isEqualTo(3L);

                assertThat(draftInvoice1.getStatus()).isEqualTo(InvoiceStatus.OPEN);
                assertThat(draftInvoice2.getStatus()).isEqualTo(InvoiceStatus.OPEN);
            }
        }
    }

    @Nested
    @DisplayName("일괄 청구서 수정")
    class BulkUpdateInvoicesTest {

        @Test
        @DisplayName("DRAFT/OPEN만 수정, PARTIAL/PAID/VOID는 skip")
        void bulkUpdate_onlyEditableSucceeds() {
            try (MockedStatic<TenantContextHolder> mockedStatic = mockStatic(TenantContextHolder.class)) {
                // Given
                mockedStatic.when(TenantContextHolder::getTenantId).thenReturn(TENANT_ID);

                Dojang mockDojang = createMockDojang();
                Invoice draftInvoice = createInvoice(1L, InvoiceStatus.DRAFT, mockDojang);
                Invoice openInvoice = createInvoice(2L, InvoiceStatus.OPEN, mockDojang);
                Invoice partialInvoice = createInvoice(3L, InvoiceStatus.PARTIAL, mockDojang);
                Invoice paidInvoice = createInvoice(4L, InvoiceStatus.PAID, mockDojang);

                given(dojangRepository.findById(DOJANG_ID)).willReturn(Optional.of(mockDojang));
                given(invoiceRepository.findAllByDojangIdAndIdIn(TENANT_ID, DOJANG_ID, List.of(1L, 2L, 3L, 4L)))
                        .willReturn(List.of(draftInvoice, openInvoice, partialInvoice, paidInvoice));

                BigDecimal newAmount = new BigDecimal("90000");
                InvoiceBulkUpdateReq request = InvoiceBulkUpdateReq.builder()
                        .invoiceIds(List.of(1L, 2L, 3L, 4L))
                        .amount(newAmount)
                        .note("할인 적용")
                        .build();

                // When
                BulkUpdateRes result = invoiceService.bulkUpdateInvoices(DOJANG_ID, request, USER_ID);

                // Then
                assertThat(result.totalCount()).isEqualTo(4);
                assertThat(result.updatedCount()).isEqualTo(2);  // DRAFT, OPEN
                assertThat(result.skippedCount()).isEqualTo(2);  // PARTIAL, PAID

                assertThat(draftInvoice.getAmount()).isEqualByComparingTo(newAmount);
                assertThat(openInvoice.getAmount()).isEqualByComparingTo(newAmount);
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

    private Invoice createInvoice(Long id, InvoiceStatus status, Dojang dojang) {
        Student student = createMockStudent(100L, "테스트학생", dojang);
        BigDecimal amount = new BigDecimal("100000");

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
                invoice.addPayment(new BigDecimal("50000"));
            }
            case PAID -> {
                invoice.issue(USER_ID);
                invoice.addPayment(amount);
            }
            case VOID -> {
                invoice.markAsVoid();
            }
        }

        ReflectionTestUtils.setField(invoice, "id", id);

        return invoice;
    }
}
