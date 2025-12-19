package com.maru.repository.invoice;

import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.service.invoice.dto.InvoiceStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByIdAndTenantIdAndDojangId(Long id, Long tenantId, Long dojangId);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.student s
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND (:status IS NULL OR i.status = :status)
        ORDER BY i.dueDate DESC
        """)
    List<Invoice> findByDojangIdWithFilters(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("status") InvoiceStatus status);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.student s
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.status IN ('OPEN', 'PARTIAL')
          AND i.dueDate = :dueDate
        """)
    List<Invoice> findUnpaidByDueDate(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("dueDate") LocalDate dueDate);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.student s
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.status IN ('OPEN', 'PARTIAL')
        ORDER BY i.dueDate ASC
        """)
    List<Invoice> findUnpaidInvoices(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId);

    @Query("""
        SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.student.id = :studentId
          AND i.issueDate >= :startDate
          AND i.issueDate < :endDate
          AND i.status != 'VOID'
        """)
    boolean existsByIssueDateRange(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    default boolean existsByDojangIdAndStudentIdAndIssueMonth(
            Long tenantId, Long dojangId, Long studentId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        return existsByIssueDateRange(tenantId, dojangId, studentId, startDate, endDate);
    }

    @Query("""
        SELECT i.student.id FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.issueDate >= :startDate
          AND i.issueDate < :endDate
          AND i.status != 'VOID'
        """)
    List<Long> findStudentIdsInDateRange(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    default List<Long> findStudentIdsWithInvoice(
            Long tenantId, Long dojangId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        return findStudentIdsInDateRange(tenantId, dojangId, startDate, endDate);
    }

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.id IN :ids
        """)
    List<Invoice> findAllByDojangIdAndIdIn(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("ids") List<Long> ids);

    @Query("""
        SELECT i FROM Invoice i
        JOIN FETCH i.student s
        WHERE i.id = :id
          AND i.tenantId = :tenantId
          AND i.dojangId = :dojangId
        """)
    Optional<Invoice> findByIdAndDojangIdWithStudent(
            @Param("id") Long id,
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId);

    @Query("""
        SELECT new com.maru.service.invoice.dto.InvoiceStatistics(
            COUNT(CASE WHEN i.status = com.maru.domain.invoice.InvoiceStatus.PAID THEN 1 END),
            COUNT(CASE WHEN i.status = com.maru.domain.invoice.InvoiceStatus.PARTIAL THEN 1 END),
            COUNT(CASE WHEN i.status IN (com.maru.domain.invoice.InvoiceStatus.OPEN, com.maru.domain.invoice.InvoiceStatus.PARTIAL) THEN 1 END),
            COALESCE(SUM(CASE WHEN i.status IN (com.maru.domain.invoice.InvoiceStatus.OPEN, com.maru.domain.invoice.InvoiceStatus.PARTIAL) THEN i.amount - i.paidAmount ELSE 0 END), 0)
        )
        FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND i.issueDate >= :startDate
          AND i.issueDate < :endDate
          AND i.status != com.maru.domain.invoice.InvoiceStatus.VOID
        """)
    InvoiceStatistics getStatistics(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

}