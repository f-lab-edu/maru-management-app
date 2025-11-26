package com.maru.repository.employment;

import com.maru.domain.employment.Employment;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Long> {
    Optional<Employment> findByUserAndDojang(User user, Dojang dojang);
}
