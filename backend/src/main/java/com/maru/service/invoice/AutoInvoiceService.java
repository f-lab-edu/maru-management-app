package com.maru.service.invoice;

import com.maru.domain.invoice.Invoice;
import com.maru.domain.student.StudentStatus;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangSettingRepository;
import com.maru.repository.tenant.view.AutoInvoiceTargetView;
import com.maru.security.TenantContextHolder;
import com.maru.service.invoice.result.AutoInvoiceResult;
import com.maru.service.invoice.result.DojangInvoiceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutoInvoiceService {

    private final DojangSettingRepository dojangSettingRepository;
    private final StudentRepository studentRepository;
    private final InvoiceRepository invoiceRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * 자동 청구서 발행 대상 도장을 조회하고 도장별로 청구서를 생성
     * 클램핑 방식: 설정일이 해당 월에 없으면 말일에 발행
     *
     * @param today 오늘 날짜
     * @param currentHour 현재 시각 (0~23)
     * @return 전체 발행 결과
     */
    public AutoInvoiceResult generateAutoInvoices(LocalDate today, int currentHour) {
        int dayOfMonth = today.getDayOfMonth();
        boolean isLastDay = today.equals(today.with(TemporalAdjusters.lastDayOfMonth()));

        List<AutoInvoiceTargetView> targets = dojangSettingRepository
                .findAllAutoInvoiceTargets(dayOfMonth, isLastDay, currentHour);

        int totalCreated = 0;
        int totalSkipped = 0;
        int failedCount = 0;

        for (AutoInvoiceTargetView target : targets) {
            try {
                DojangInvoiceResult result = processOneDojang(target);
                totalCreated += result.created();
                totalSkipped += result.skipped();
            } catch (Exception e) {
                failedCount++;
                log.error("자동 청구서 발행 실패: dojangId={}", target.getDojangId(), e);
            }
        }

        return AutoInvoiceResult.of(targets.size(), totalCreated, totalSkipped, failedCount);
    }

    private DojangInvoiceResult processOneDojang(AutoInvoiceTargetView target) throws Exception {
        try (AutoCloseable ignored = TenantContextHolder.withContext(
                target.getTenantId(), TenantContextHolder.SYSTEM_USER_ID,
                target.getDojangId(), TenantContextHolder.ROLE_OWNER)) {

            return transactionTemplate.execute(status -> {
                YearMonth billingMonth = YearMonth.now();
                BigDecimal amount = BigDecimal.valueOf(target.getDefaultTuition());
                LocalDate dueDate = billingMonth.atEndOfMonth();

                List<String> eligibleStudentIds = findEligibleStudentIds(target, billingMonth);

                List<Invoice> invoices = createInvoices(
                        target, eligibleStudentIds, billingMonth, amount, dueDate);
                invoiceRepository.saveAll(invoices);

                int skipped = eligibleStudentIds.size() - invoices.size();
                return new DojangInvoiceResult(invoices.size(), skipped);
            });
        }
    }

    private List<String> findEligibleStudentIds(AutoInvoiceTargetView target, YearMonth billingMonth) {
        List<String> activeStudentIds = studentRepository
                .findActiveStudentIdsByDojang(target.getTenantId(), target.getDojangId(), StudentStatus.WITHDRAWN);

        List<String> alreadyInvoiced = invoiceRepository
                .findStudentIdsWithInvoiceByBillingYearMonth(
                        target.getTenantId(), target.getDojangId(), billingMonth);
        Set<String> excludeIds = new HashSet<>(alreadyInvoiced);

        return activeStudentIds.stream()
                .filter(id -> !excludeIds.contains(id))
                .toList();
    }

    private List<Invoice> createInvoices(AutoInvoiceTargetView target, List<String> studentIds,
                                         YearMonth billingMonth, BigDecimal amount, LocalDate dueDate) {
        return studentIds.stream()
                .map(studentId -> Invoice.create(
                        target.getTenantId(), target.getDojangId(), studentId,
                        billingMonth, amount, dueDate, null))
                .toList();
    }
}
