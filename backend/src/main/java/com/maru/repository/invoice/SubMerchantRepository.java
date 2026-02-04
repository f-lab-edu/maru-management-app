package com.maru.repository.invoice;

import com.maru.domain.invoice.SubMerchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubMerchantRepository extends JpaRepository<SubMerchant, String> {

    Optional<SubMerchant> findByDojangId(String dojangId);

    Optional<SubMerchant> findByTossSellerId(String tossSellerId);
}
