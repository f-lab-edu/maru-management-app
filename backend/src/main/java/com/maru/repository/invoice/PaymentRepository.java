package com.maru.repository.invoice;

import com.maru.domain.invoice.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Query("""
        SELECT p FROM Payment p
        WHERE p.tenantId = :tenantId
          AND p.dojangId = :dojangId
          AND p.invoice.id = :invoiceId
        ORDER BY p.paidAt DESC
        """)
    List<Payment> findByInvoiceIdOrderByPaidAtDesc(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("invoiceId") String invoiceId);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
        WHERE p.tenantId = :tenantId
          AND p.dojangId = :dojangId
          AND p.status = 'PAID'
          AND p.paidAt >= :startDateTime
          AND p.paidAt < :endDateTime
        """)
    BigDecimal sumByTenantIdAndPeriod(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.invoice i
        WHERE p.tenantId = :tenantId
          AND p.dojangId = :dojangId
          AND i.studentId = :studentId
        ORDER BY p.paidAt DESC
        """)
    List<Payment> findByStudentIdOrderByPaidAtDesc(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentId") String studentId);

    Optional<Payment> findByIdAndTenantIdAndDojangId(String id, String tenantId, String dojangId);

    @Query("""
        SELECT COUNT(p) FROM Payment p
        WHERE p.tenantId = :tenantId
          AND p.dojangId = :dojangId
          AND p.status = 'PAID'
          AND p.paidAt >= :startDateTime
          AND p.paidAt < :endDateTime
        """)
    int countByTenantIdAndPeriod(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}
