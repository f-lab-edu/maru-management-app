package com.maru.repository.guardian;

import com.maru.domain.guardian.Guardianship;
import com.maru.repository.guardian.view.BroadcastRecipientView;
import com.maru.repository.guardian.view.GuardianStudentNameView;
import com.maru.repository.guardian.view.GuardianshipView;
import com.maru.repository.guardian.view.PrimaryGuardianView;
import com.maru.repository.guardian.view.StudentGuardianIdView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuardianshipRepository extends JpaRepository<Guardianship, String> {

    Optional<Guardianship> findByStudentIdAndGuardianIdAndDeletedAtIsNull(String studentId, String guardianId);

    @Query("""
            SELECT g.studentId as studentId,
                   g.guardian.name as guardianName,
                   g.guardian.phone as guardianPhone
            FROM Guardianship g
            WHERE g.studentId IN :studentIds
              AND g.isPrimary = true
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    List<PrimaryGuardianView> findPrimaryGuardianViewsByStudentIds(@Param("studentIds") List<String> studentIds);

    @Query("""
            SELECT g.guardian.id as guardianId,
                   g.guardian.name as guardianName,
                   g.guardian.phone as guardianPhone,
                   g.guardian.isVerified as isVerified,
                   g.relation as relation,
                   g.isPrimary as isPrimary
            FROM Guardianship g
            WHERE g.studentId = :studentId
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
            WHERE g.studentId = :studentId
              AND g.guardian.id = :guardianId
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    Optional<GuardianshipView> findGuardianshipByStudentIdAndGuardianId(
            @Param("studentId") String studentId,
            @Param("guardianId") String guardianId);

    @Query("""
            SELECT g.guardian.id
            FROM Guardianship g
            WHERE g.studentId = :studentId
              AND g.isPrimary = true
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    List<String> findPrimaryGuardianIdsByStudentId(@Param("studentId") String studentId);

    @Query("""
            SELECT g.studentId as studentId, g.guardian.id as guardianId
            FROM Guardianship g
            WHERE g.studentId IN :studentIds
              AND g.isPrimary = true
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    List<StudentGuardianIdView> findPrimaryGuardianIdsByStudentIds(
            @Param("studentIds") List<String> studentIds);

    @Query("""
            SELECT g.studentId as studentId,
                   g.guardian.id as guardianId,
                   g.guardian.name as guardianName,
                   g.guardian.phone as guardianPhone
            FROM Guardianship g
            WHERE g.studentId IN :studentIds
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
            """)
    List<BroadcastRecipientView> findGuardiansByStudentIds(@Param("studentIds") List<String> studentIds);

    @Query("""
            SELECT g.guardian.id as guardianId,
                   s.name as studentName
            FROM Guardianship g
            JOIN Student s ON s.id = g.studentId
            WHERE g.guardian.id IN :guardianIds
              AND g.deletedAt IS NULL
              AND g.guardian.deletedAt IS NULL
              AND s.deletedAt IS NULL
            ORDER BY g.isPrimary DESC
            """)
    List<GuardianStudentNameView> findStudentNamesByGuardianIds(@Param("guardianIds") List<String> guardianIds);
}
