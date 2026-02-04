package com.maru.repository.invoice;

import com.maru.domain.invoice.PaymentLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentLinkRepository extends JpaRepository<PaymentLink, String> {

    Optional<PaymentLink> findByToken(String token);

    Optional<PaymentLink> findByInvoiceIdAndUsedAtIsNull(String invoiceId);
}
