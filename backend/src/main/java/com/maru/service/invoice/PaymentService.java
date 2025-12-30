package com.maru.service.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.InvoiceErrorCode;
import com.maru.common.exception.PaymentErrorCode;
import com.maru.controller.invoice.dto.*;
import com.maru.domain.guardian.Guardian;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.PaymentStatus;
import com.maru.domain.student.Student;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.PaymentRepository;
import com.maru.repository.invoice.projection.InvoiceStatistics;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final DojangRepository dojangRepository;
    private final StudentRepository studentRepository;
    private final GuardianshipRepository guardianshipRepository;

    /**
     * 수납 기록
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @param request 수납 기록 요청
     * @param userId 현재 사용자 ID
     * @return 수납 기록 후 청구서 상세 정보
     * @throws BusinessException AMOUNT_EXCEEDS_REMAINING - 수납 금액이 남은 금액 초과
     */
    @Transactional
    public InvoiceDetailRes recordPayment(String dojangId, String invoiceId, PaymentRecordReq request, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Invoice invoice = findInvoiceWithStudent(invoiceId, tenantId, dojangId);
        Payment payment = createAndSavePayment(invoice, request, userId);
        invoice.addPayment(payment.getAmount());

        log.info("수납 기록 완료: invoiceId={}, amount={}, method={}, userId={}",
                invoiceId, request.amount(), request.method(), userId);

        return buildInvoiceDetailRes(invoice, invoiceId);
    }

    /**
     * 수납 취소 (환불)
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @param paymentId 수납 ID
     * @param userId 현재 사용자 ID
     * @return 환불 후 청구서 상세 정보
     * @throws BusinessException NOT_FOUND - 수납 내역을 찾을 수 없음
     * @throws BusinessException ALREADY_REFUNDED - 이미 환불된 수납
     */
    @Transactional
    public InvoiceDetailRes cancelPayment(String dojangId, String invoiceId, String paymentId, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Invoice invoice = findInvoiceWithStudent(invoiceId, tenantId, dojangId);
        Payment payment = findPaymentAndValidate(paymentId, invoiceId, tenantId, dojangId);

        processRefund(payment, invoice);

        log.info("수납 취소 완료: paymentId={}, invoiceId={}, amount={}, userId={}",
                paymentId, invoiceId, payment.getAmount(), userId);

        return buildInvoiceDetailRes(invoice, invoiceId);
    }

    /**
     * 미납자 목록 조회
     *
     * @param dojangId 도장 ID
     * @return 미납 청구서 목록 (연체일 포함)
     */
    @Transactional(readOnly = true)
    public List<UnpaidListRes> getUnpaidList(String dojangId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        List<Invoice> unpaidInvoices = invoiceRepository.findUnpaidInvoices(tenantId, dojangId);

        return buildUnpaidListResponses(unpaidInvoices);
    }

    /**
     * 수납 통계 조회 (청구 연월 기준)
     *
     * @param dojangId 도장 ID
     * @param year 청구 연도
     * @param month 청구 월 (1-12)
     * @return 수납 통계 (완납/미납/부분납 건수 및 금액)
     */
    @Transactional(readOnly = true)
    public PaymentStatisticsRes getPaymentStatistics(String dojangId, int year, int month) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        BigDecimal totalPaidAmount = paymentRepository.sumByTenantIdAndPeriod(
                tenantId, dojangId, startOfMonth, startOfNextMonth);

        InvoiceStatistics statistics = invoiceRepository.getStatistics(
                tenantId, dojangId, year, month);

        return PaymentStatisticsRes.builder()
                .totalPaidAmount(totalPaidAmount)
                .totalUnpaidAmount(statistics.getTotalUnpaidAmount())
                .paidInvoiceCount((int) statistics.getPaidCount())
                .unpaidInvoiceCount((int) statistics.getUnpaidCount())
                .partialInvoiceCount((int) statistics.getPartialCount())
                .build();
    }

    /**
     * 원생별 납부 내역 조회
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @return 원생의 납부 이력
     * @throws BusinessException NOT_FOUND - 원생을 찾을 수 없음
     */
    @Transactional(readOnly = true)
    public StudentPaymentHistoryRes getStudentPaymentHistory(String dojangId, String studentId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = findStudentAndValidate(studentId, dojangId);
        List<Payment> payments = paymentRepository.findByStudentIdOrderByPaidAtDesc(tenantId, dojangId, studentId);

        return buildStudentPaymentHistoryRes(student, payments);
    }

    private void validateDojangAccess(String dojangId, String tenantId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        if (!dojang.getTenant().getId().equals(tenantId)) {
            throw new BusinessException(DojangErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Invoice findInvoiceWithStudent(String invoiceId, String tenantId, String dojangId) {
        return invoiceRepository.findByIdAndDojangIdWithStudent(invoiceId, tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(InvoiceErrorCode.NOT_FOUND));
    }

    private Payment createAndSavePayment(Invoice invoice, PaymentRecordReq request, String userId) {
        Payment payment = Payment.create(invoice, request.amount(), request.method(), userId);
        return paymentRepository.save(payment);
    }

    private Payment findPaymentAndValidate(String paymentId, String invoiceId, String tenantId, String dojangId) {
        Payment payment = paymentRepository.findByIdAndTenantIdAndDojangId(paymentId, tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.NOT_FOUND));

        if (!payment.getInvoice().getId().equals(invoiceId)) {
            throw new BusinessException(PaymentErrorCode.NOT_FOUND);
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BusinessException(PaymentErrorCode.ALREADY_REFUNDED);
        }

        return payment;
    }

    private void processRefund(Payment payment, Invoice invoice) {
        payment.refund();
        invoice.subtractPayment(payment.getAmount());
    }

    private InvoiceDetailRes buildInvoiceDetailRes(Invoice invoice, String invoiceId) {
        List<PaymentRes> payments = paymentRepository.findByInvoiceIdOrderByPaidAtDesc(invoiceId)
                .stream()
                .map(PaymentRes::from)
                .toList();

        return InvoiceDetailRes.from(invoice, payments);
    }

    private List<UnpaidListRes> buildUnpaidListResponses(List<Invoice> unpaidInvoices) {
        if (unpaidInvoices.isEmpty()) {
            return List.of();
        }

        LocalDate today = LocalDate.now();
        Map<String, Guardian> guardianMap = fetchPrimaryGuardiansAsMap(unpaidInvoices);

        return unpaidInvoices.stream()
                .map(invoice -> buildUnpaidListRes(invoice, today, guardianMap))
                .toList();
    }

    private Map<String, Guardian> fetchPrimaryGuardiansAsMap(List<Invoice> invoices) {
        List<String> studentIds = invoices.stream()
                .map(invoice -> invoice.getStudent().getId())
                .toList();

        return guardianshipRepository.findPrimaryGuardianshipsByStudentIds(studentIds)
                .stream()
                .collect(Collectors.toMap(
                        g -> g.getStudent().getId(),
                        g -> g.getGuardian(),
                        (g1, g2) -> g1
                ));
    }

    private UnpaidListRes buildUnpaidListRes(Invoice invoice, LocalDate today, Map<String, Guardian> guardianMap) {
        Student student = invoice.getStudent();
        Guardian primaryGuardian = guardianMap.get(student.getId());

        return UnpaidListRes.builder()
                .invoiceId(invoice.getId())
                .studentName(student.getName())
                .guardianName(primaryGuardian != null ? primaryGuardian.getName() : null)
                .guardianPhone(primaryGuardian != null ? primaryGuardian.getPhone() : null)
                .amount(invoice.getAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .dueDate(invoice.getDueDate())
                .overdueDays(calculateOverdueDays(invoice.getDueDate(), today))
                .build();
    }

    private int calculateOverdueDays(LocalDate dueDate, LocalDate today) {
        if (dueDate.isBefore(today)) {
            return (int) ChronoUnit.DAYS.between(dueDate, today);
        }
        return 0;
    }

    private Student findStudentAndValidate(String studentId, String dojangId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));

        if (!student.getDojang().getId().equals(dojangId)) {
            throw new BusinessException(StudentErrorCode.NOT_FOUND);
        }

        return student;
    }

    private StudentPaymentHistoryRes buildStudentPaymentHistoryRes(Student student, List<Payment> payments) {
        BigDecimal totalPaidAmount = calculateTotalPaidAmount(payments);
        List<StudentPaymentHistoryRes.PaymentHistoryItem> historyItems = buildPaymentHistoryItems(payments);

        return StudentPaymentHistoryRes.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .totalPaidAmount(totalPaidAmount)
                .payments(historyItems)
                .build();
    }

    private BigDecimal calculateTotalPaidAmount(List<Payment> payments) {
        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<StudentPaymentHistoryRes.PaymentHistoryItem> buildPaymentHistoryItems(List<Payment> payments) {
        return payments.stream()
                .map(this::toPaymentHistoryItem)
                .toList();
    }

    private StudentPaymentHistoryRes.PaymentHistoryItem toPaymentHistoryItem(Payment payment) {
        Invoice invoice = payment.getInvoice();
        return StudentPaymentHistoryRes.PaymentHistoryItem.builder()
                .paymentId(payment.getId())
                .invoiceId(invoice.getId())
                .billingYear(invoice.getBillingYear())
                .billingMonth(invoice.getBillingMonth())
                .amount(payment.getAmount())
                .method(payment.getMethod() != null ? payment.getMethod().name() : null)
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paidAt(payment.getPaidAt() != null ? payment.getPaidAt().format(DATE_TIME_FORMATTER) : null)
                .refundedAt(payment.getRefundedAt() != null ? payment.getRefundedAt().format(DATE_TIME_FORMATTER) : null)
                .build();
    }
}
