package com.maru.service.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.controller.invoice.dto.*;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final DojangRepository dojangRepository;
    private final StudentRepository studentRepository;

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

        var student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));

        if (!student.getDojang().getId().equals(dojangId)) {
            throw new BusinessException(StudentErrorCode.NOT_FOUND);
        }

        throw new UnsupportedOperationException("Repository 구현 필요");
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

        throw new UnsupportedOperationException("Repository 구현 필요");
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

        throw new UnsupportedOperationException("Repository 구현 필요");
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

        throw new UnsupportedOperationException("Repository 구현 필요");
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

        throw new UnsupportedOperationException("Repository 구현 필요");
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

        throw new UnsupportedOperationException("Repository 구현 필요");
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

        throw new UnsupportedOperationException("Repository 구현 필요");
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

        throw new UnsupportedOperationException("Repository 구현 필요");
    }

    private void validateDojangAccess(Long dojangId, Long tenantId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        if (!dojang.getTenant().getId().equals(tenantId)) {
            throw new BusinessException(DojangErrorCode.UNAUTHORIZED_ACCESS);
        }

    }
}