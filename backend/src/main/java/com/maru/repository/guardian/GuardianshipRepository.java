package com.maru.repository.guardian;

import com.maru.domain.guardian.Guardian;
import com.maru.domain.guardian.Guardianship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuardianshipRepository extends JpaRepository<Guardianship, String> {

    List<Guardianship> findByStudentIdAndDeletedAtIsNull(String studentId);

    Optional<Guardianship> findByStudentIdAndIsPrimaryTrueAndDeletedAtIsNull(String studentId);

    Optional<Guardianship> findByStudentIdAndGuardianIdAndDeletedAtIsNull(String studentId, String guardianId);

    @Query("""
            SELECT g.guardian
            FROM Guardianship g
            WHERE g.student.id = :studentId
              AND (:primaryOnly = false OR g.isPrimary = true)
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    List<Guardian> findGuardiansByStudentId(
            @Param("studentId") String studentId,
            @Param("primaryOnly") boolean primaryOnly
    );

    @Query("""
            SELECT g FROM Guardianship g
            JOIN FETCH g.guardian
            WHERE g.student.id IN :studentIds
              AND g.isPrimary = true
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    List<Guardianship> findPrimaryGuardianshipsByStudentIds(@Param("studentIds") List<String> studentIds);
}
