package com.maru.service.invoice;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.domain.permission.PermissionType;
import com.maru.security.RequirePermission;
import com.maru.controller.invoice.dto.*;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.domain.invoice.exception.InvoiceErrorCode;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.security.TenantContextHolder;
import com.maru.service.enrollment.EnrollmentQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@ValidateDojangAccess
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentQueryService enrollmentQueryService;
    private final InvoiceQueryService invoiceQueryService;

    /**
     * 단일 청구서 생성
     *
     * @param dojangId 도장 ID
     * @param request 청구서 생성 요청
     * @param userId 현재 사용자 ID
     * @return 생성된 청구서 상세 정보
     * @throws BusinessException DUPLICATE_INVOICE - 동일 월 중복 청구서
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public InvoiceDetailRes createInvoice(String dojangId, InvoiceCreateReq request, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
                validateStudentInDojang(request.studentId(), dojangId);

        YearMonth billingYearMonth = resolveBillingYearMonth(request.billingYearMonth());
        BigDecimal amount = resolveAmount(request.amount());
        Invoice invoice = createAndSaveInvoice(tenantId, dojangId, request.studentId(),
                billingYearMonth, amount, request.dueDate(), request.note());

        log.info("청구서 생성 완료: invoiceId={}, studentId={}, billingYearMonth={}",
                invoice.getId(), request.studentId(), billingYearMonth);

        return invoiceQueryService.getInvoice(tenantId, dojangId, invoice.getId());
    }

    /**
     * 일괄 청구서 생성
     *
     * @param dojangId 도장 ID
     * @param request 일괄 생성 요청
     * @param userId 현재 사용자 ID
     * @return 생성 결과 (생성 수, 스킵 수)
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public BulkCreateRes createBulkInvoices(String dojangId, InvoiceBulkCreateReq request, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        YearMonth billingYearMonth = resolveBillingYearMonth(request.billingYearMonth());
        BigDecimal amount = resolveAmount(request.defaultAmount());

        TargetStudentIdsResult targetResult = findAllTargetStudentIds(tenantId, dojangId, request);
        List<String> eligibleStudentIds = excludeAlreadyInvoiced(
                targetResult.studentIds, tenantId, dojangId, billingYearMonth);

        int createdCount = createInvoicesForStudents(tenantId, dojangId, eligibleStudentIds,
                billingYearMonth, amount, request.dueDate(), request.note());
        int skippedCount = targetResult.totalCount - createdCount;

        log.info("일괄 청구서 생성 완료: dojangId={}, billingYearMonth={}, created={}, skipped={}",
                dojangId, billingYearMonth, createdCount, skippedCount);

        return BulkCreateRes.builder()
                .createdCount(createdCount)
                .skippedCount(skippedCount)
                .build();
    }

    /**
     * 청구서 목록 조회
     *
     * @param dojangId 도장 ID
     * @param status 상태 필터 (null이면 전체)
     * @param sectionId 수련부 ID (선택, 해당 수련부 소속 원생만 조회)
     * @param divisionId 수련반 ID (선택, 해당 수련반 소속 원생만 조회)
     * @return 청구서 목록
     */
    @RequirePermission(PermissionType.PAYMENT_VIEW)
    @Transactional(readOnly = true)
    public List<InvoiceListRes> getInvoices(String dojangId, InvoiceStatus status,
                                             String sectionId, String divisionId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        List<String> filteredStudentIds = enrollmentQueryService.getStudentIdsByFilter(dojangId, sectionId, divisionId);

        return invoiceQueryService.getInvoices(tenantId, dojangId, status, filteredStudentIds);
    }

    /**
     * 청구서 상세 조회
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @return 청구서 상세 정보 (수납 내역 포함)
     * @throws BusinessException NOT_FOUND - 청구서를 찾을 수 없음
     */
    @RequirePermission(PermissionType.PAYMENT_VIEW)
    @Transactional(readOnly = true)
    public InvoiceDetailRes getInvoice(String dojangId, String invoiceId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        return invoiceQueryService.getInvoice(tenantId, dojangId, invoiceId);
    }

    /**
     * 청구서 수정 (DRAFT 상태만 가능)
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @param request 수정 요청
     * @param userId 현재 사용자 ID
     * @return 수정된 청구서 상세 정보
     * @throws BusinessException CANNOT_UPDATE_NON_DRAFT - DRAFT 상태가 아닌 경우
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public InvoiceDetailRes updateInvoice(String dojangId, String invoiceId, InvoiceUpdateReq request, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        Invoice invoice = findInvoice(invoiceId, tenantId, dojangId);
        invoice.update(request.amount(), request.dueDate(), request.note());

        log.info("청구서 수정 완료: invoiceId={}, userId={}", invoiceId, userId);

        return invoiceQueryService.getInvoice(tenantId, dojangId, invoiceId);
    }

    /**
     * 청구서 발행 (DRAFT → OPEN)
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @param userId 현재 사용자 ID
     * @return 발행된 청구서 상세 정보
     * @throws BusinessException INVALID_STATUS_TRANSITION - 잘못된 상태 전이
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public InvoiceDetailRes issueInvoice(String dojangId, String invoiceId, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        Invoice invoice = findInvoice(invoiceId, tenantId, dojangId);
        invoice.issue(userId);

        log.info("청구서 발행 완료: invoiceId={}, userId={}", invoiceId, userId);

        return invoiceQueryService.getInvoice(tenantId, dojangId, invoiceId);
    }

    /**
     * 청구서 무효화 (DRAFT/OPEN → VOID)
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @param userId 현재 사용자 ID
     * @return 무효화된 청구서 상세 정보
     * @throws BusinessException CANNOT_VOID_PAID_INVOICE - PAID 상태에서 무효화 시도
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public InvoiceDetailRes voidInvoice(String dojangId, String invoiceId, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        Invoice invoice = findInvoice(invoiceId, tenantId, dojangId);
        invoice.markAsVoid();

        log.info("청구서 무효화 완료: invoiceId={}, userId={}", invoiceId, userId);

        return invoiceQueryService.getInvoice(tenantId, dojangId, invoiceId);
    }

    /**
     * 청구서 복구 (VOID → DRAFT)
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @param userId 현재 사용자 ID
     * @return 복구된 청구서 상세 정보
     * @throws BusinessException CANNOT_RESTORE_NON_VOID - VOID 상태가 아닌 경우
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public InvoiceDetailRes restoreInvoice(String dojangId, String invoiceId, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        Invoice invoice = findInvoice(invoiceId, tenantId, dojangId);
        invoice.restore();

        log.info("청구서 복구 완료: invoiceId={}, userId={}", invoiceId, userId);

        return invoiceQueryService.getInvoice(tenantId, dojangId, invoiceId);
    }

    /**
     * 청구서 삭제 (DRAFT 상태만 가능)
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @param userId 현재 사용자 ID
     * @throws BusinessException CANNOT_DELETE_NON_DRAFT - DRAFT 상태가 아닌 경우
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public void deleteInvoice(String dojangId, String invoiceId, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        Invoice invoice = findInvoice(invoiceId, tenantId, dojangId);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException(InvoiceErrorCode.CANNOT_DELETE_NON_DRAFT);
        }

        invoiceRepository.delete(invoice);
        log.info("청구서 삭제 완료: invoiceId={}, userId={}", invoiceId, userId);
    }

    /**
     * 청구서 일괄 발행 (DRAFT → OPEN)
     *
     * @param dojangId 도장 ID
     * @param request 일괄 발행 요청 (invoiceIds)
     * @param userId 현재 사용자 ID
     * @return 발행 결과 (성공 수, 실패 수)
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public BulkIssueRes bulkIssueInvoices(String dojangId, BulkIssueReq request, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        List<Invoice> invoices = invoiceRepository.findAllByDojangIdAndIdIn(tenantId, dojangId, request.invoiceIds());
        List<BulkIssueRes.FailedInvoice> failedInvoices = new ArrayList<>();
        int issuedCount = issueInvoicesAndCollectFailures(invoices, userId, failedInvoices);

        log.info("일괄 발행 완료: dojangId={}, issued={}, failed={}",
                dojangId, issuedCount, failedInvoices.size());

        return BulkIssueRes.builder()
                .issuedCount(issuedCount)
                .failedCount(failedInvoices.size())
                .failedInvoices(failedInvoices)
                .build();
    }

    /**
     * 청구서 일괄 수정 (DRAFT, OPEN 상태만 가능)
     *
     * @param dojangId 도장 ID
     * @param request 일괄 수정 요청
     * @param userId 현재 사용자 ID
     * @return 수정 결과 (전체 수, 성공 수, 스킵 수)
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public BulkUpdateRes bulkUpdateInvoices(String dojangId, InvoiceBulkUpdateReq request, String userId) {
        String tenantId = TenantContextHolder.getTenantId();
        
        List<Invoice> invoices = invoiceRepository.findAllByDojangIdAndIdIn(tenantId, dojangId, request.invoiceIds());

        int updatedCount = 0;
        for (Invoice invoice : invoices) {
            if (!invoice.isEditable()) {
                continue;
            }
            invoice.update(request.amount(), request.dueDate(), request.note());
            updatedCount++;
        }

        log.info("일괄 수정 완료: dojangId={}, updated={}, skipped={}",
                dojangId, updatedCount, request.invoiceIds().size() - updatedCount);

        return BulkUpdateRes.of(request.invoiceIds().size(), updatedCount);
    }

    private void validateStudentInDojang(String studentId, String dojangId) {
        Student student = studentRepository.findByIdAndDojangId(studentId, dojangId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));
        if (student.isDeleted()) {
            throw new BusinessException(StudentErrorCode.WITHDRAWN);
        }
    }

    private BigDecimal resolveAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private YearMonth resolveBillingYearMonth(YearMonth yearMonth) {
        return yearMonth != null ? yearMonth : YearMonth.now();
    }

    private record TargetStudentIdsResult(List<String> studentIds, int totalCount) {}

    private TargetStudentIdsResult findAllTargetStudentIds(String tenantId, String dojangId, InvoiceBulkCreateReq request) {
        if (request.studentIds() != null && !request.studentIds().isEmpty()) {
            int totalCount = request.studentIds().size();
            List<String> studentIds = studentRepository.findActiveStudentIdsByIdsAndDojang(
                    request.studentIds(), tenantId, dojangId, StudentStatus.WITHDRAWN);
            return new TargetStudentIdsResult(studentIds, totalCount);
        }

        List<String> studentIds = studentRepository.findActiveStudentIdsByDojang(tenantId, dojangId, StudentStatus.WITHDRAWN);
        return new TargetStudentIdsResult(studentIds, studentIds.size());
    }

    private List<String> excludeAlreadyInvoiced(List<String> studentIds, String tenantId, String dojangId,
                                                 YearMonth billingYearMonth) {
        List<String> existingStudentIds = invoiceRepository.findStudentIdsWithInvoiceByBillingYearMonth(
                tenantId, dojangId, billingYearMonth);
        Set<String> excludeIds = new HashSet<>(existingStudentIds);

        return studentIds.stream()
                .filter(id -> !excludeIds.contains(id))
                .toList();
    }

    private Invoice createAndSaveInvoice(String tenantId, String dojangId, String studentId,
                                         YearMonth billingYearMonth, BigDecimal amount,
                                         LocalDate dueDate, String note) {
        boolean exists = invoiceRepository.existsByBillingYearMonth(tenantId, dojangId, studentId, billingYearMonth);
        if (exists) {
            throw new BusinessException(InvoiceErrorCode.DUPLICATE_INVOICE);
        }

        Invoice invoice = Invoice.create(tenantId, dojangId, studentId, billingYearMonth, amount, dueDate, note);
        return invoiceRepository.save(invoice);
    }

    private int createInvoicesForStudents(String tenantId, String dojangId, List<String> studentIds,
                                          YearMonth billingYearMonth, BigDecimal amount,
                                          LocalDate dueDate, String note) {
        List<Invoice> invoices = studentIds.stream()
                .map(studentId -> Invoice.create(tenantId, dojangId, studentId, billingYearMonth, amount, dueDate, note))
                .toList();
        invoiceRepository.saveAll(invoices);
        return invoices.size();
    }

    private Invoice findInvoice(String invoiceId, String tenantId, String dojangId) {
        return invoiceRepository.findByIdAndTenantIdAndDojangId(invoiceId, tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(InvoiceErrorCode.NOT_FOUND));
    }

    private int issueInvoicesAndCollectFailures(List<Invoice> invoices, String userId,
                                                  List<BulkIssueRes.FailedInvoice> failedInvoices) {
        int issuedCount = 0;
        for (Invoice invoice : invoices) {
            String failureReason = tryIssueInvoice(invoice, userId);
            if (failureReason == null) {
                issuedCount++;
            } else {
                failedInvoices.add(BulkIssueRes.FailedInvoice.builder()
                        .invoiceId(invoice.getId())
                        .reason(failureReason)
                        .build());
            }
        }
        return issuedCount;
    }

    private String tryIssueInvoice(Invoice invoice, String userId) {
        try {
            invoice.issue(userId);
            return null;
        } catch (BusinessException e) {
            log.warn("청구서 발행 실패: invoiceId={}, reason={}", invoice.getId(), e.getMessage());
            return e.getMessage();
        }
    }
}
