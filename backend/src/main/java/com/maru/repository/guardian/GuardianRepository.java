package com.maru.repository.guardian;

import com.maru.domain.guardian.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    Optional<Guardian> findByPhoneAndDeletedAtIsNull(String phone);
}
