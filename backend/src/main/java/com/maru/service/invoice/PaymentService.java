package com.maru.service.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.domain.permission.PermissionType;
import com.maru.security.RequirePermission;
import com.maru.domain.invoice.exception.InvoiceErrorCode;
import com.maru.domain.invoice.exception.PaymentErrorCode;
import com.maru.controller.invoice.dto.*;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.PaymentMethod;
import com.maru.domain.invoice.PaymentStatus;
import com.maru.domain.student.Student;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.repository.student.view.StudentMinimalView;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.PaymentRepository;
import com.maru.repository.invoice.view.InvoiceStatisticsView;
import com.maru.repository.invoice.view.MonthlyInvoiceStatisticsView;
import com.maru.repository.student.StudentRepository;
import com.maru.common.aop.ValidateDojangAccess;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ValidateDojangAccess
public class PaymentService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;
    private final InvoiceQueryService invoiceQueryService;
    private final PaymentQueryService paymentQueryService;
    private final RefundService refundService;

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
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public InvoiceDetailRes recordPayment(String dojangId, String invoiceId, PaymentRecordReq request, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        Invoice invoice = findInvoice(invoiceId, tenantId, dojangId);
        Payment payment = createAndSavePayment(invoice, request, userId);
        invoice.addPayment(payment.getAmount());

        log.info("수납 기록 완료: invoiceId={}, amount={}, method={}, userId={}",
                invoiceId, request.amount(), request.method(), userId);

        return buildInvoiceDetailRes(invoice);
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
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public InvoiceDetailRes cancelPayment(String dojangId, String invoiceId, String paymentId, String userId) {
        refundService.refundFull(dojangId, paymentId);

        String tenantId = TenantContextHolder.getTenantId();
        Invoice invoice = findInvoice(invoiceId, tenantId, dojangId);

        log.info("수납 취소 완료: paymentId={}, invoiceId={}, userId={}",
                paymentId, invoiceId, userId);

        return buildInvoiceDetailRes(invoice);
    }

    /**
     * 미납자 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택)
     * @param divisionId 수련반 ID (선택)
     * @return 미납 청구서 목록 (연체일 포함)
     */
    @RequirePermission(PermissionType.PAYMENT_VIEW)
    @Transactional(readOnly = true)
    public List<UnpaidListRes> getUnpaidList(String dojangId, String sectionId, String divisionId) {
        String tenantId = TenantContextHolder.getTenantId();
        return paymentQueryService.getUnpaidList(tenantId, dojangId, sectionId, divisionId);
    }

    /**
     * 수납 통계 조회 (청구 연월 기준)
     *
     * @param dojangId 도장 ID
     * @param year 청구 연도
     * @param month 청구 월 (1-12)
     * @return 수납 통계 (완납/미납/부분납 건수 및 금액)
     */
    @RequirePermission(PermissionType.PAYMENT_VIEW)
    @Transactional(readOnly = true)
    public PaymentStatisticsRes getPaymentStatistics(String dojangId, int year, int month) {
        String tenantId = TenantContextHolder.getTenantId();
        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        BigDecimal totalPaidAmount = paymentRepository.sumByTenantIdAndPeriod(
                tenantId, dojangId, startOfMonth, startOfNextMonth);

        InvoiceStatisticsView statistics = invoiceRepository.getStatistics(
                tenantId, dojangId, YearMonth.of(year, month));

        return PaymentStatisticsRes.builder()
                .totalPaidAmount(totalPaidAmount)
                .totalUnpaidAmount(statistics.getTotalUnpaidAmount())
                .paidInvoiceCount((int) statistics.getPaidCount())
                .unpaidInvoiceCount((int) statistics.getUnpaidCount())
                .partialInvoiceCount((int) statistics.getPartialCount())
                .build();
    }

    /**
     * 연간 수납 통계 조회
     *
     * @param dojangId 도장 ID
     * @param year 조회 연도
     * @return 연간 통계 (월별 데이터 포함)
     */
    @RequirePermission(PermissionType.PAYMENT_VIEW)
    @Transactional(readOnly = true)
    public YearlyStatisticsRes getYearStatistics(String dojangId, int year) {
        String tenantId = TenantContextHolder.getTenantId();
        YearMonth startYearMonth = YearMonth.of(year, 1);
        YearMonth endYearMonth = YearMonth.of(year, 12);

        List<MonthlyInvoiceStatisticsView> queryResult = invoiceRepository.getYearStatistics(
                tenantId, dojangId, startYearMonth, endYearMonth);

        List<MonthlyStatisticsData> monthlyData = buildMonthlyDataWithZeroFilling(year, queryResult);

        BigDecimal totalPaidAmount = monthlyData.stream()
                .map(MonthlyStatisticsData::paidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalUnpaidAmount = monthlyData.stream()
                .map(MonthlyStatisticsData::unpaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int paidCount = monthlyData.stream().mapToInt(MonthlyStatisticsData::paidCount).sum();
        int unpaidCount = monthlyData.stream().mapToInt(MonthlyStatisticsData::unpaidCount).sum();
        int partialCount = monthlyData.stream().mapToInt(MonthlyStatisticsData::partialCount).sum();

        return YearlyStatisticsRes.builder()
                .year(year)
                .totalPaidAmount(totalPaidAmount)
                .totalUnpaidAmount(totalUnpaidAmount)
                .paidInvoiceCount(paidCount)
                .unpaidInvoiceCount(unpaidCount)
                .partialInvoiceCount(partialCount)
                .monthlyData(monthlyData)
                .build();
    }

    private List<MonthlyStatisticsData> buildMonthlyDataWithZeroFilling(
            int year, List<MonthlyInvoiceStatisticsView> queryResult) {
        Map<Integer, MonthlyInvoiceStatisticsView> resultMap = queryResult.stream()
                .collect(Collectors.toMap(
                        stat -> stat.getYearMonth().getMonthValue(),
                        stat -> stat
                ));

        List<MonthlyStatisticsData> monthlyData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            MonthlyInvoiceStatisticsView stat = resultMap.get(month);
            if (stat != null) {
                monthlyData.add(new MonthlyStatisticsData(
                        month,
                        stat.getTotalAmount(),
                        stat.getTotalPaidAmount(),
                        stat.getTotalUnpaidAmount(),
                        stat.getPaidCount(),
                        stat.getUnpaidCount(),
                        stat.getPartialCount()
                ));
            } else {
                monthlyData.add(MonthlyStatisticsData.zero(month));
            }
        }
        return monthlyData;
    }

    /**
     * 원생별 납부 내역 조회
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @return 원생의 납부 이력
     * @throws BusinessException NOT_FOUND - 원생을 찾을 수 없음
     */
    @RequirePermission(PermissionType.PAYMENT_VIEW)
    @Transactional(readOnly = true)
    public StudentPaymentHistoryRes getStudentPaymentHistory(String dojangId, String studentId) {
        String tenantId = TenantContextHolder.getTenantId();
        StudentMinimalView student = findStudentAndValidate(dojangId, studentId);
        List<Payment> payments = paymentRepository.findByStudentIdOrderByPaidAtDesc(tenantId, dojangId, studentId);

        return buildStudentPaymentHistoryRes(student, payments);
    }

    /**
     * 선납 수납 처리 (여러 월 청구서 생성 + 일괄 수납)
     *
     * @param dojangId 도장 ID
     * @param request 선납 수납 요청
     * @param userId 현재 사용자 ID
     * @return 선납 처리 결과
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public PrepaidPaymentRes processPrepaidPayment(String dojangId, PrepaidPaymentReq request, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateStudentInDojang(request.studentId(), dojangId);
        List<YearMonth> months = calculateMonthRange(
                request.startYearMonth(), request.endYearMonth()
        );
        validateNoDuplicateInvoices(tenantId, dojangId, request.studentId(), months);

        List<String> invoiceIds = createInvoicesWithPayments(tenantId, dojangId, request.studentId(), months, request, userId);

        log.info("선납 수납 완료: studentId={}, months={}, totalAmount={}, userId={}",
                request.studentId(), months.size(), request.totalAmount(), userId);

        return buildPrepaidResponse(months.size(), request, invoiceIds);
    }

    private Invoice findInvoice(String invoiceId, String tenantId, String dojangId) {
        return invoiceRepository.findByIdAndTenantIdAndDojangId(invoiceId, tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(InvoiceErrorCode.NOT_FOUND));
    }

    private Payment createAndSavePayment(Invoice invoice, PaymentRecordReq request, String userId) {
        Payment payment = Payment.create(invoice, request.amount(), request.method(), userId);
        return paymentRepository.save(payment);
    }

    private InvoiceDetailRes buildInvoiceDetailRes(Invoice invoice) {
        return invoiceQueryService.getInvoice(invoice.getTenantId(), invoice.getDojangId(), invoice.getId());
    }

    private void validateStudentInDojang(String studentId, String dojangId) {
        Student student = studentRepository.findByIdAndDojangId(studentId, dojangId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));
        if (student.isDeleted()) {
            throw new BusinessException(StudentErrorCode.WITHDRAWN);
        }
    }

    private StudentMinimalView findStudentAndValidate(String dojangId, String studentId) {
        return studentRepository.findMinimalByIdAndDojangId(studentId, dojangId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));
    }

    private StudentPaymentHistoryRes buildStudentPaymentHistoryRes(StudentMinimalView student, List<Payment> payments) {
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
                .billingYearMonth(invoice.getBillingYearMonth())
                .amount(payment.getAmount())
                .method(payment.getMethod() != null ? payment.getMethod().name() : null)
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paidAt(payment.getPaidAt() != null ? payment.getPaidAt().format(DATE_TIME_FORMATTER) : null)
                .refundedAt(payment.getRefundedAt() != null ? payment.getRefundedAt().format(DATE_TIME_FORMATTER) : null)
                .build();
    }


    private List<YearMonth> calculateMonthRange(YearMonth start, YearMonth end) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = start;

        while (!current.isAfter(end)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        return months;
    }


    private void validateNoDuplicateInvoices(String tenantId, String dojangId, String studentId, List<YearMonth> months) {
        for (YearMonth ym : months) {
            boolean exists = invoiceRepository.existsByBillingYearMonth(
                    tenantId, dojangId, studentId, ym
            );
            if (exists) {
                throw new BusinessException(InvoiceErrorCode.PREPAID_DUPLICATE_INVOICE);
            }
        }
    }

    private List<String> createInvoicesWithPayments(
            String tenantId,
            String dojangId,
            String studentId,
            List<YearMonth> months,
            PrepaidPaymentReq request,
            String userId
    ) {
        int monthCount = months.size();
        BigDecimal amountPerInvoice = request.totalAmount()
                .divide(BigDecimal.valueOf(monthCount), 0, RoundingMode.DOWN);
        BigDecimal remainder = request.totalAmount()
                .subtract(amountPerInvoice.multiply(BigDecimal.valueOf(monthCount)));

        List<String> invoiceIds = new ArrayList<>();
        LocalDate dueDate = request.dueDate() != null ? request.dueDate() : LocalDate.now();

        for (int i = 0; i < months.size(); i++) {
            BigDecimal invoiceAmount = (i == 0)
                    ? amountPerInvoice.add(remainder)
                    : amountPerInvoice;

            String invoiceId = createSingleInvoiceWithPayment(
                    tenantId, dojangId, studentId, months.get(i), invoiceAmount, dueDate, request.note(), request.paymentMethod(), userId
            );
            invoiceIds.add(invoiceId);
        }

        return invoiceIds;
    }

    private String createSingleInvoiceWithPayment(
            String tenantId,
            String dojangId,
            String studentId,
            YearMonth yearMonth,
            BigDecimal amount,
            LocalDate dueDate,
            String note,
            PaymentMethod paymentMethod,
            String userId
    ) {
        Invoice invoice = Invoice.create(
                tenantId,
                dojangId,
                studentId,
                yearMonth,
                amount,
                dueDate,
                note
        );
        invoice.issue(userId);
        invoiceRepository.save(invoice);

        Payment payment = Payment.create(invoice, amount, paymentMethod, userId);
        paymentRepository.save(payment);
        invoice.addPayment(amount);

        return invoice.getId();
    }

    private PrepaidPaymentRes buildPrepaidResponse(int monthCount, PrepaidPaymentReq request, List<String> invoiceIds) {
        BigDecimal originalTotal = request.monthlyAmount().multiply(BigDecimal.valueOf(monthCount));
        BigDecimal discountAmount = originalTotal.subtract(request.totalAmount());

        return PrepaidPaymentRes.builder()
                .invoiceCount(monthCount)
                .totalAmount(request.totalAmount())
                .discountAmount(discountAmount)
                .invoiceIds(invoiceIds)
                .build();
    }
}
