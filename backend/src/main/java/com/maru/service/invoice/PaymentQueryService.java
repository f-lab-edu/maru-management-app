package com.maru.service.invoice;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.controller.guardian.dto.GuardianInfo;
import com.maru.controller.invoice.dto.UnpaidListRes;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.guardian.view.PrimaryGuardianView;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.view.UnpaidInvoiceView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ValidateDojangAccess
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final InvoiceRepository invoiceRepository;
    private final GuardianshipRepository guardianshipRepository;

    /**
     * 미납자 목록 조회
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택)
     * @param divisionId 수련반 ID (선택)
     * @return 미납 청구서 목록 (연체일 포함)
     */
    public List<UnpaidListRes> getUnpaidList(String tenantId, String dojangId,
                                              String sectionId, String divisionId) {
        List<UnpaidInvoiceView> views = invoiceRepository.findUnpaidForPayment(
                tenantId, dojangId, sectionId, divisionId);

        if (views.isEmpty()) {
            return List.of();
        }

        List<String> studentIds = views.stream()
                .map(UnpaidInvoiceView::getStudentId)
                .distinct()
                .toList();

        Map<String, List<GuardianInfo>> guardianMap = fetchPrimaryGuardiansMap(studentIds);
        LocalDate today = LocalDate.now();

        return views.stream()
                .map(view -> toUnpaidListRes(view, guardianMap.getOrDefault(view.getStudentId(), List.of()), today))
                .toList();
    }

    private Map<String, List<GuardianInfo>> fetchPrimaryGuardiansMap(List<String> studentIds) {
        return guardianshipRepository.findPrimaryGuardianViewsByStudentIds(studentIds)
                .stream()
                .collect(Collectors.groupingBy(
                        PrimaryGuardianView::getStudentId,
                        Collectors.mapping(this::toGuardianInfo, Collectors.toList())
                ));
    }

    private GuardianInfo toGuardianInfo(PrimaryGuardianView view) {
        return GuardianInfo.builder()
                .name(view.getGuardianName())
                .phone(view.getGuardianPhone())
                .build();
    }

    private UnpaidListRes toUnpaidListRes(UnpaidInvoiceView view, List<GuardianInfo> guardians, LocalDate today) {
        return UnpaidListRes.builder()
                .invoiceId(view.getInvoiceId())
                .studentName(view.getStudentName())
                .guardians(guardians)
                .amount(view.getAmount())
                .paidAmount(view.getPaidAmount())
                .remainingAmount(view.getAmount().subtract(view.getPaidAmount()))
                .dueDate(view.getDueDate())
                .overdueDays(calculateOverdueDays(view.getDueDate(), today))
                .billingYearMonth(view.getBillingYearMonth())
                .build();
    }

    private int calculateOverdueDays(LocalDate dueDate, LocalDate today) {
        if (dueDate.isBefore(today)) {
            return (int) ChronoUnit.DAYS.between(dueDate, today);
        }
        return 0;
    }
}
