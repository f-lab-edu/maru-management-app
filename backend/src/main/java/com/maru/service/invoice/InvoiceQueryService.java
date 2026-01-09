package com.maru.service.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.InvoiceErrorCode;
import com.maru.controller.invoice.dto.InvoiceDetailRes;
import com.maru.controller.invoice.dto.InvoiceListRes;
import com.maru.controller.invoice.dto.PaymentRes;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.PaymentRepository;
import com.maru.repository.invoice.view.InvoiceStudentView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceQueryService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 청구서 단건 조회
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @return 청구서 상세 정보
     * @throws BusinessException NOT_FOUND - 청구서를 찾을 수 없는 경우
     */
    public InvoiceDetailRes getInvoice(String tenantId, String dojangId, String invoiceId) {
        InvoiceStudentView view = invoiceRepository.findDetailById(invoiceId, tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(InvoiceErrorCode.NOT_FOUND));

        List<PaymentRes> payments = paymentRepository.findByInvoiceIdOrderByPaidAtDesc(tenantId, dojangId, invoiceId)
                .stream()
                .map(PaymentRes::from)
                .toList();

        return toInvoiceDetailRes(view, payments);
    }

    /**
     * 청구서 목록 조회
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param status 상태 필터 (null이면 전체)
     * @param filteredStudentIds 필터링된 원생 ID 목록 (null이면 전체)
     * @return 청구서 목록
     */
    public List<InvoiceListRes> getInvoices(String tenantId, String dojangId,
                                             InvoiceStatus status, List<String> filteredStudentIds) {
        List<InvoiceStudentView> views = fetchInvoiceViews(tenantId, dojangId, status, filteredStudentIds);
        return views.stream()
                .map(this::toInvoiceListRes)
                .toList();
    }

    /**
     * 미수납 청구서 목록 조회
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택)
     * @param divisionId 수련반 ID (선택)
     * @return 미수납 청구서 목록
     */
    public List<InvoiceListRes> getUnpaidInvoices(String tenantId, String dojangId,
                                                   String sectionId, String divisionId) {
        return invoiceRepository.findUnpaidWithStudent(tenantId, dojangId, sectionId, divisionId)
                .stream()
                .map(this::toInvoiceListRes)
                .toList();
    }

    private List<InvoiceStudentView> fetchInvoiceViews(String tenantId, String dojangId,
                                                        InvoiceStatus status, List<String> filteredStudentIds) {
        if (filteredStudentIds != null) {
            if (filteredStudentIds.isEmpty()) {
                return List.of();
            }
            return invoiceRepository.findAllWithStudentByStudentIds(tenantId, dojangId, filteredStudentIds, status);
        }
        return invoiceRepository.findAllWithStudent(tenantId, dojangId, status);
    }

    private InvoiceListRes toInvoiceListRes(InvoiceStudentView view) {
        return InvoiceListRes.builder()
                .id(view.getId())
                .studentName(view.getStudentName())
                .amount(view.getAmount())
                .paidAmount(view.getPaidAmount())
                .remainingAmount(view.getAmount().subtract(view.getPaidAmount()))
                .status(view.getStatus())
                .dueDate(view.getDueDate())
                .issueDate(view.getIssueDate())
                .billingYearMonth(view.getBillingYearMonth())
                .build();
    }

    private InvoiceDetailRes toInvoiceDetailRes(InvoiceStudentView view, List<PaymentRes> payments) {
        return InvoiceDetailRes.builder()
                .id(view.getId())
                .studentId(view.getStudentId())
                .studentName(view.getStudentName())
                .amount(view.getAmount())
                .paidAmount(view.getPaidAmount())
                .remainingAmount(view.getAmount().subtract(view.getPaidAmount()))
                .status(view.getStatus())
                .dueDate(view.getDueDate())
                .issueDate(view.getIssueDate())
                .note(view.getNote())
                .billingYearMonth(view.getBillingYearMonth())
                .payments(payments)
                .build();
    }
}
