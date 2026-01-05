package com.maru.repository.enrollment;

import com.maru.domain.enrollment.Enrollment;
import com.maru.repository.enrollment.view.EnrollmentStudentView;
import com.maru.repository.enrollment.view.StudentCountByDivisionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    @Query("""
            SELECT e.studentId AS studentId, s.name AS studentName, e.createdAt AS createdAt
            FROM Enrollment e
            JOIN Student s ON e.studentId = s.id
            WHERE e.dojangId = :dojangId
            AND e.divisionId = :divisionId
            ORDER BY s.name
            """)
    List<EnrollmentStudentView> findAllWithStudentByDivisionId(
            @Param("dojangId") String dojangId,
            @Param("divisionId") String divisionId);

    boolean existsByDojangIdAndDivisionIdAndStudentId(String dojangId, String divisionId, String studentId);

    Optional<Enrollment> findByDojangIdAndDivisionIdAndStudentId(String dojangId, String divisionId, String studentId);

    @Query("""
            SELECT COUNT(e) FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.divisionId = :divisionId
            """)
    int countByDivisionId(
            @Param("dojangId") String dojangId,
            @Param("divisionId") String divisionId);

    @Query("""
            SELECT e.studentId FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.divisionId = :divisionId
            AND e.studentId IN :studentIds
            """)
    List<String> findAlreadyEnrolledStudentIds(
            @Param("dojangId") String dojangId,
            @Param("divisionId") String divisionId,
            @Param("studentIds") List<String> studentIds);

    @Modifying
    @Query("""
            DELETE FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.divisionId = :divisionId
            """)
    void deleteAllByDivisionId(@Param("dojangId") String dojangId, @Param("divisionId") String divisionId);

    @Query("""
            SELECT e.divisionId AS divisionId, COUNT(e) AS studentCount
            FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.divisionId IN :divisionIds
            GROUP BY e.divisionId
            """)
    List<StudentCountByDivisionView> countStudentsByDivisionIds(
            @Param("dojangId") String dojangId,
            @Param("divisionIds") List<String> divisionIds);

    @Query("""
            SELECT DISTINCT e.studentId FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.studentId IN :studentIds
            """)
    List<String> findEnrolledStudentIds(
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds);

    @Query("""
            SELECT DISTINCT e.studentId FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.divisionId = :divisionId
            """)
    List<String> findStudentIdsByDivisionId(
            @Param("dojangId") String dojangId,
            @Param("divisionId") String divisionId);

    @Query("""
            SELECT DISTINCT e.studentId FROM Enrollment e
            JOIN Division d ON e.divisionId = d.id
            WHERE e.dojangId = :dojangId
            AND d.section.id = :sectionId
            """)
    List<String> findStudentIdsBySectionId(
            @Param("dojangId") String dojangId,
            @Param("sectionId") String sectionId);
}
