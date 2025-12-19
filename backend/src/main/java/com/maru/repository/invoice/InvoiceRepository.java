package com.maru.repository.invoice;

import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.InvoiceStatus;
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
          AND FUNCTION('YEAR', i.issueDate) = :year
          AND FUNCTION('MONTH', i.issueDate) = :month
          AND i.status != 'VOID'
        """)
    boolean existsByDojangIdAndStudentIdAndIssueMonth(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("studentId") Long studentId,
            @Param("year") int year,
            @Param("month") int month);

    @Query("""
        SELECT i.student.id FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.dojangId = :dojangId
          AND FUNCTION('YEAR', i.issueDate) = :year
          AND FUNCTION('MONTH', i.issueDate) = :month
          AND i.status != 'VOID'
        """)
    List<Long> findStudentIdsWithInvoice(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("year") int year,
            @Param("month") int month);

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
}