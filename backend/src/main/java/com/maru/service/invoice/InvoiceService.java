package com.maru.service.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.InvoiceErrorCode;
import com.maru.controller.invoice.dto.*;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.PaymentRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;

    /**
     * 단일 청구서 생성
     *
     * @param dojangId 도장 ID
     * @param request 청구서 생성 요청
     * @param userId 현재 사용자 ID
     * @return 생성된 청구서 상세 정보
     * @throws BusinessException DUPLICATE_INVOICE - 동일 월 중복 청구서
     */
    @Transactional
    public InvoiceDetailRes createInvoice(Long dojangId, InvoiceCreateReq request, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Student student = findStudentAndValidate(request.studentId(), dojangId);
        validateNoDuplicateInvoice(tenantId, dojangId, request.studentId());
        validateAmountRequired(request.amount());

        Invoice invoice = createAndSaveInvoice(student, request);

        log.info("청구서 생성 완료: invoiceId={}, studentId={}, amount={}",
                invoice.getId(), request.studentId(), request.amount());

        return buildInvoiceDetailRes(invoice);
    }

    /**
     * 일괄 청구서 생성
     *
     * @param dojangId 도장 ID
     * @param request 일괄 생성 요청
     * @param userId 현재 사용자 ID
     * @return 생성 결과 (생성 수, 스킵 수)
     */
    @Transactional
    public BulkCreateRes createBulkInvoices(Long dojangId, InvoiceBulkCreateReq request, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);
        validateAmountRequired(request.defaultAmount());

        List<Student> targetStudents = findTargetStudentsForBulkCreate(tenantId, dojangId, request);
        int createdCount = createInvoicesForStudents(targetStudents, request);

        int totalActiveStudents = studentRepository.findActiveStudents(tenantId, dojangId, StudentStatus.WITHDRAWN).size();
        int skippedCount = totalActiveStudents - createdCount;

        log.info("일괄 청구서 생성 완료: dojangId={}, created={}, skipped={}",
                dojangId, createdCount, skippedCount);

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
     * @return 청구서 목록
     */
    @Transactional(readOnly = true)
    public List<InvoiceListRes> getInvoices(Long dojangId, InvoiceStatus status) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        List<Invoice> invoices = invoiceRepository.findByDojangIdWithFilters(tenantId, dojangId, status);

        return invoices.stream()
                .map(InvoiceListRes::from)
                .toList();
    }

    /**
     * 청구서 상세 조회
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @return 청구서 상세 정보 (수납 내역 포함)
     * @throws BusinessException NOT_FOUND - 청구서를 찾을 수 없음
     */
    @Transactional(readOnly = true)
    public InvoiceDetailRes getInvoice(Long dojangId, Long invoiceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Invoice invoice = findInvoiceWithStudent(invoiceId, tenantId, dojangId);

        return buildInvoiceDetailRes(invoice);
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
    @Transactional
    public InvoiceDetailRes updateInvoice(Long dojangId, Long invoiceId, InvoiceUpdateReq request, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Invoice invoice = findInvoiceWithStudent(invoiceId, tenantId, dojangId);
        invoice.update(request.amount(), request.dueDate(), request.note());

        log.info("청구서 수정 완료: invoiceId={}, userId={}", invoiceId, userId);

        return buildInvoiceDetailRes(invoice);
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
    @Transactional
    public InvoiceDetailRes issueInvoice(Long dojangId, Long invoiceId, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Invoice invoice = findInvoiceWithStudent(invoiceId, tenantId, dojangId);
        invoice.issue(userId);

        log.info("청구서 발행 완료: invoiceId={}, userId={}", invoiceId, userId);

        return buildInvoiceDetailRes(invoice);
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
    @Transactional
    public InvoiceDetailRes voidInvoice(Long dojangId, Long invoiceId, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        Invoice invoice = findInvoiceWithStudent(invoiceId, tenantId, dojangId);
        invoice.markAsVoid();

        log.info("청구서 무효화 완료: invoiceId={}, userId={}", invoiceId, userId);

        return buildInvoiceDetailRes(invoice);
    }

    /**
     * 청구서 일괄 발행 (DRAFT → OPEN)
     *
     * @param dojangId 도장 ID
     * @param request 일괄 발행 요청 (invoiceIds)
     * @param userId 현재 사용자 ID
     * @return 발행 결과 (성공 수, 실패 수)
     */
    @Transactional
    public BulkIssueRes bulkIssueInvoices(Long dojangId, BulkIssueReq request, Long userId) {
        Long tenantId = TenantContextHolder.getTenantId();
        validateDojangAccess(dojangId, tenantId);

        List<Invoice> invoices = invoiceRepository.findAllByDojangIdAndIdIn(tenantId, dojangId, request.invoiceIds());
        int[] result = issueInvoicesAndCountResult(invoices, userId);

        log.info("일괄 발행 완료: dojangId={}, issued={}, failed={}",
                dojangId, result[0], result[1]);

        return BulkIssueRes.builder()
                .issuedCount(result[0])
                .failedCount(result[1])
                .build();
    }

    private void validateDojangAccess(Long dojangId, Long tenantId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        if (!dojang.getTenant().getId().equals(tenantId)) {
            throw new BusinessException(DojangErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Student findStudentAndValidate(Long studentId, Long dojangId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));

        if (!student.getDojang().getId().equals(dojangId)) {
            throw new BusinessException(StudentErrorCode.NOT_FOUND);
        }

        return student;
    }

    private void validateNoDuplicateInvoice(Long tenantId, Long dojangId, Long studentId) {
        LocalDate now = LocalDate.now();
        boolean exists = invoiceRepository.existsByDojangIdAndStudentIdAndIssueMonth(
                tenantId, dojangId, studentId, now.getYear(), now.getMonthValue());

        if (exists) {
            throw new BusinessException(InvoiceErrorCode.DUPLICATE_INVOICE);
        }
    }

    private void validateAmountRequired(java.math.BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException(InvoiceErrorCode.AMOUNT_REQUIRED);
        }
    }

    private Invoice createAndSaveInvoice(Student student, InvoiceCreateReq request) {
        Invoice invoice = Invoice.create(student, request.amount(), request.dueDate(), request.note());
        return invoiceRepository.save(invoice);
    }

    private List<Student> findTargetStudentsForBulkCreate(Long tenantId, Long dojangId, InvoiceBulkCreateReq request) {
        List<Student> activeStudents = studentRepository.findActiveStudents(tenantId, dojangId, StudentStatus.WITHDRAWN);

        LocalDate now = LocalDate.now();
        List<Long> existingInvoiceStudentIds = invoiceRepository.findStudentIdsWithInvoice(
                tenantId, dojangId, now.getYear(), now.getMonthValue());

        Set<Long> excludeIds = buildExcludeStudentIds(request.excludeStudentIds(), existingInvoiceStudentIds);

        return activeStudents.stream()
                .filter(student -> !excludeIds.contains(student.getId()))
                .toList();
    }

    private Set<Long> buildExcludeStudentIds(List<Long> requestExcludeIds, List<Long> existingInvoiceStudentIds) {
        Set<Long> excludeIds = new HashSet<>(existingInvoiceStudentIds);
        if (requestExcludeIds != null) {
            excludeIds.addAll(requestExcludeIds);
        }
        return excludeIds;
    }

    private int createInvoicesForStudents(List<Student> students, InvoiceBulkCreateReq request) {
        int createdCount = 0;
        for (Student student : students) {
            Invoice invoice = Invoice.create(student, request.defaultAmount(), request.dueDate(), request.note());
            invoiceRepository.save(invoice);
            createdCount++;
        }
        return createdCount;
    }

    private Invoice findInvoiceWithStudent(Long invoiceId, Long tenantId, Long dojangId) {
        return invoiceRepository.findByIdAndDojangIdWithStudent(invoiceId, tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(InvoiceErrorCode.NOT_FOUND));
    }

    private InvoiceDetailRes buildInvoiceDetailRes(Invoice invoice) {
        List<PaymentRes> payments = paymentRepository.findByInvoiceIdOrderByPaidAtDesc(invoice.getId())
                .stream()
                .map(PaymentRes::from)
                .toList();

        return InvoiceDetailRes.from(invoice, payments);
    }

    private int[] issueInvoicesAndCountResult(List<Invoice> invoices, Long userId) {
        int issuedCount = 0;
        int failedCount = 0;

        for (Invoice invoice : invoices) {
            if (tryIssueInvoice(invoice, userId)) {
                issuedCount++;
            } else {
                failedCount++;
            }
        }

        return new int[] { issuedCount, failedCount };
    }

    private boolean tryIssueInvoice(Invoice invoice, Long userId) {
        try {
            invoice.issue(userId);
            return true;
        } catch (BusinessException e) {
            log.warn("청구서 발행 실패: invoiceId={}, reason={}", invoice.getId(), e.getMessage());
            return false;
        }
    }
}
