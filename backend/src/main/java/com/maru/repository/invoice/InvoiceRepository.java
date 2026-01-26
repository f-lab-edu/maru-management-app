package com.maru.repository.invoice;

import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.repository.invoice.view.InvoiceStatisticsView;
import com.maru.repository.invoice.view.InvoiceStudentView;
import com.maru.repository.invoice.view.MonthlyInvoiceStatisticsView;
import com.maru.repository.invoice.view.RecentPaymentView;
import com.maru.repository.invoice.view.UnpaidInvoiceView;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    Optional<Invoice> findByIdAndTenantIdAndDojangId(String id, String tenantId, String dojangId);

    @Query("""
        SELECT i.id as id, i.studentId as studentId, s.name as studentName,
               i.amount as amount, i.paidAmount as paidAmount, i.status as status,
               i.dueDate as dueDate, i.issueDate as issueDate, i.note as note,
               i.billingYearMonth as billingYearMonth,
               CASE WHEN s.deletedAt IS NOT NULL THEN true ELSE false END as studentDeleted
        FROM Invoice i
        JOIN Student s ON i.studentId = s.id
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND (:status IS NULL OR i.status = :status)
        ORDER BY i.dueDate DESC
        """)
    List<InvoiceStudentView> findAllWithStudent(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("status") InvoiceStatus status);

    @Query("""
        SELECT i.id as id, i.studentId as studentId, s.name as studentName,
               i.amount as amount, i.paidAmount as paidAmount, i.status as status,
               i.dueDate as dueDate, i.issueDate as issueDate, i.note as note,
               i.billingYearMonth as billingYearMonth,
               CASE WHEN s.deletedAt IS NOT NULL THEN true ELSE false END as studentDeleted
        FROM Invoice i
        JOIN Student s ON i.studentId = s.id
        LEFT JOIN Enrollment e ON e.studentId = s.id
            AND e.dojangId = :dojangId
        LEFT JOIN Division d ON e.divisionId = d.id
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.status IN ('OPEN', 'PARTIAL')
          AND s.deletedAt IS NULL
          AND (:sectionId IS NULL OR d.section.id = :sectionId)
          AND (:divisionId IS NULL OR e.divisionId = :divisionId)
        ORDER BY i.dueDate ASC
        """)
    List<InvoiceStudentView> findUnpaidWithStudent(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("sectionId") String sectionId,
            @Param("divisionId") String divisionId);

    @Query("""
        SELECT i.id as invoiceId, s.id as studentId, s.name as studentName,
               i.amount as amount, i.paidAmount as paidAmount,
               i.dueDate as dueDate, i.billingYearMonth as billingYearMonth
        FROM Invoice i
        JOIN Student s ON i.studentId = s.id
        LEFT JOIN Enrollment e ON e.studentId = s.id AND e.dojangId = :dojangId
        LEFT JOIN Division d ON e.divisionId = d.id
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.status IN ('OPEN', 'PARTIAL')
          AND s.deletedAt IS NULL
          AND (:sectionId IS NULL OR d.section.id = :sectionId)
          AND (:divisionId IS NULL OR e.divisionId = :divisionId)
        ORDER BY i.dueDate ASC
        """)
    List<UnpaidInvoiceView> findUnpaidForPayment(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("sectionId") String sectionId,
            @Param("divisionId") String divisionId);

    @Query("""
        SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.studentId = :studentId
          AND i.billingYearMonth = :yearMonth
        """)
    boolean existsByBillingYearMonth(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentId") String studentId,
            @Param("yearMonth") YearMonth yearMonth);

    @Query("""
        SELECT i.studentId FROM Invoice i
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
        SELECT i.id as id, i.studentId as studentId, s.name as studentName,
               i.amount as amount, i.paidAmount as paidAmount, i.status as status,
               i.dueDate as dueDate, i.issueDate as issueDate, i.note as note,
               i.billingYearMonth as billingYearMonth,
               CASE WHEN s.deletedAt IS NOT NULL THEN true ELSE false END as studentDeleted
        FROM Invoice i
        JOIN Student s ON i.studentId = s.id
        WHERE i.id = :id
          AND i.tenantId = :tenantId
          AND i.dojangId = :dojangId
        """)
    Optional<InvoiceStudentView> findDetailById(
            @Param("id") String id,
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId);

    @Query("""
        SELECT
            COUNT(CASE WHEN i.status = 'PAID' THEN 1 END) AS paidCount,
            COUNT(CASE WHEN i.status = 'PARTIAL' THEN 1 END) AS partialCount,
            COUNT(CASE WHEN i.status IN ('OPEN', 'PARTIAL') THEN 1 END) AS unpaidCount,
            COALESCE(SUM(i.amount), 0) AS totalAmount,
            COALESCE(SUM(i.paidAmount), 0) AS totalPaidAmount,
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
        SELECT i.id as id, i.studentId as studentId, s.name as studentName,
               i.amount as amount, i.paidAmount as paidAmount, i.status as status,
               i.dueDate as dueDate, i.issueDate as issueDate, i.note as note,
               i.billingYearMonth as billingYearMonth,
               CASE WHEN s.deletedAt IS NOT NULL THEN true ELSE false END as studentDeleted
        FROM Invoice i
        JOIN Student s ON i.studentId = s.id
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.studentId IN :studentIds
          AND (:status IS NULL OR i.status = :status)
        ORDER BY i.dueDate DESC
        """)
    List<InvoiceStudentView> findAllWithStudentByStudentIds(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("status") InvoiceStatus status);

    @Query("""
        SELECT i.studentId as studentId, s.name as studentName, i.updatedAt as paidAt
        FROM Invoice i
        JOIN Student s ON i.studentId = s.id
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.status = 'PAID'
          AND i.updatedAt >= :since
        ORDER BY i.updatedAt DESC
        """)
    List<RecentPaymentView> findRecentPayments(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("since") LocalDateTime since);
}
