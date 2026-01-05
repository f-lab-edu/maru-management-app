package com.maru.repository.invoice;

import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.repository.invoice.view.InvoiceStatisticsView;
import com.maru.repository.invoice.view.MonthlyInvoiceStatisticsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    Optional<Invoice> findByIdAndTenantIdAndDojangId(String id, String tenantId, String dojangId);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.student s
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND (:status IS NULL OR i.status = :status)
        ORDER BY i.dueDate DESC
        """)
    List<Invoice> findByDojangIdWithFilters(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("status") InvoiceStatus status);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.student s
        LEFT JOIN Enrollment e ON e.student.id = s.id
            AND e.division.dojangId = :dojangId
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.status IN ('OPEN', 'PARTIAL')
          AND (:sectionId IS NULL OR e.division.section.id = :sectionId)
          AND (:divisionId IS NULL OR e.division.id = :divisionId)
        ORDER BY i.dueDate ASC
        """)
    List<Invoice> findUnpaidInvoices(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("sectionId") String sectionId,
            @Param("divisionId") String divisionId);

    @Query("""
        SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.student.id = :studentId
          AND i.billingYearMonth = :yearMonth
        """)
    boolean existsByBillingYearMonth(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentId") String studentId,
            @Param("yearMonth") YearMonth yearMonth);

    @Query("""
        SELECT i.student.id FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.billingYearMonth = :yearMonth
        """)
    List<String> findStudentIdsWithInvoiceByBillingYearMonth(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("yearMonth") YearMonth yearMonth);

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.id IN :ids
        """)
    List<Invoice> findAllByDojangIdAndIdIn(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("ids") List<String> ids);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.student s
        WHERE i.id = :id
          AND i.tenantId = :tenantId
          AND i.dojangId = :dojangId
        """)
    Optional<Invoice> findByIdAndDojangIdWithStudent(
            @Param("id") String id,
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId);

    @Query("""
        SELECT
            COUNT(CASE WHEN i.status = 'PAID' THEN 1 END) AS paidCount,
            COUNT(CASE WHEN i.status = 'PARTIAL' THEN 1 END) AS partialCount,
            COUNT(CASE WHEN i.status IN ('OPEN', 'PARTIAL') THEN 1 END) AS unpaidCount,
            COALESCE(SUM(CASE WHEN i.status IN ('OPEN', 'PARTIAL') THEN i.amount - i.paidAmount ELSE 0 END), 0) AS totalUnpaidAmount
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.billingYearMonth = :yearMonth
          AND i.status != 'VOID'
        """)
    InvoiceStatisticsView getStatistics(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("yearMonth") YearMonth yearMonth);

    @Query("""
        SELECT
            i.billingYearMonth AS yearMonth,
            COALESCE(SUM(i.amount), 0) AS totalAmount,
            COALESCE(SUM(i.paidAmount), 0) AS totalPaidAmount,
            COALESCE(SUM(CASE WHEN i.status IN ('OPEN', 'PARTIAL') THEN i.amount - i.paidAmount ELSE 0 END), 0) AS totalUnpaidAmount,
            COUNT(CASE WHEN i.status = 'PAID' THEN 1 END) AS paidCount,
            COUNT(CASE WHEN i.status = 'PARTIAL' THEN 1 END) AS partialCount,
            COUNT(CASE WHEN i.status IN ('OPEN', 'PARTIAL') THEN 1 END) AS unpaidCount
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.billingYearMonth >= :startYearMonth
          AND i.billingYearMonth <= :endYearMonth
          AND i.status != 'VOID'
        GROUP BY i.billingYearMonth
        ORDER BY i.billingYearMonth
        """)
    List<MonthlyInvoiceStatisticsView> getYearStatistics(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("startYearMonth") YearMonth startYearMonth,
            @Param("endYearMonth") YearMonth endYearMonth);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.student s
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.student.id IN :studentIds
          AND (:status IS NULL OR i.status = :status)
        ORDER BY i.dueDate DESC
        """)
    List<Invoice> findByDojangIdWithFiltersAndStudentIds(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("status") InvoiceStatus status);
}
