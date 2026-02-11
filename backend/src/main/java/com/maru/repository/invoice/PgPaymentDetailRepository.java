package com.maru.repository.invoice;

import com.maru.domain.invoice.PgPaymentDetail;
import com.maru.domain.invoice.PgPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PgPaymentDetailRepository extends JpaRepository<PgPaymentDetail, String> {

    Optional<PgPaymentDetail> findByPaymentKey(String paymentKey);

    Optional<PgPaymentDetail> findByOrderId(String orderId);

    Optional<PgPaymentDetail> findByPaymentId(String paymentId);

    List<PgPaymentDetail> findByStatus(PgPaymentStatus status);

    List<PgPaymentDetail> findAllByPaymentIdIn(List<String> paymentIds);
}
