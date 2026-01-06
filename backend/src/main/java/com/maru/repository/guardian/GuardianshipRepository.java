package com.maru.repository.guardian;

import com.maru.domain.guardian.Guardian;
import com.maru.domain.guardian.Guardianship;
import com.maru.repository.guardian.view.GuardianshipView;
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

    @Query("""
            SELECT g.guardian.id as guardianId,
                   g.guardian.name as guardianName,
                   g.guardian.phone as guardianPhone,
                   g.guardian.isVerified as isVerified,
                   g.relation as relation,
                   g.isPrimary as isPrimary
            FROM Guardianship g
            WHERE g.student.id = :studentId
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            ORDER BY g.isPrimary DESC, g.createdAt ASC
            """)
    List<GuardianshipView> findGuardianshipsByStudentId(@Param("studentId") String studentId);

    @Query("""
            SELECT g.guardian.id as guardianId,
                   g.guardian.name as guardianName,
                   g.guardian.phone as guardianPhone,
                   g.guardian.isVerified as isVerified,
                   g.relation as relation,
                   g.isPrimary as isPrimary
            FROM Guardianship g
            WHERE g.student.id = :studentId
              AND g.guardian.id = :guardianId
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    Optional<GuardianshipView> findGuardianshipByStudentIdAndGuardianId(
            @Param("studentId") String studentId,
            @Param("guardianId") String guardianId);
}
