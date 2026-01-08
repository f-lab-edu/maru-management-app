package com.maru.repository.guardian;

import com.maru.domain.guardian.Guardian;
import com.maru.repository.guardian.view.GuardianPhoneView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuardianRepository extends JpaRepository<Guardian, String> {

    Optional<Guardian> findByPhoneAndDeletedAtIsNull(String phone);

    @Query("""
            SELECT g.id as id, g.phone as phone
            FROM Guardian g
            WHERE g.id IN :ids
            """)
    List<GuardianPhoneView> findPhonesByIds(@Param("ids") List<String> ids);
}
