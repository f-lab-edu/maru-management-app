package com.maru.repository.tenant;

import com.maru.domain.tenant.Dojang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DojangRepository extends JpaRepository<Dojang, Long> {
}
