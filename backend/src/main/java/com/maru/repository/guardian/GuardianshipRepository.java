package com.maru.repository.guardian;

import com.maru.domain.guardian.Guardian;
import com.maru.domain.guardian.Guardianship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuardianshipRepository extends JpaRepository<Guardianship, Long> {

    List<Guardianship> findByStudentIdAndDeletedAtIsNull(Long studentId);

    Optional<Guardianship> findByStudentIdAndIsPrimaryTrueAndDeletedAtIsNull(Long studentId);

    Optional<Guardianship> findByStudentIdAndGuardianIdAndDeletedAtIsNull(Long studentId, Long guardianId);

    @Query("""
            SELECT g.guardian
            FROM Guardianship g
            WHERE g.student.id = :studentId
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    List<Guardian> findGuardiansByStudentId(@Param("studentId") Long studentId);
}
