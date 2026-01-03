package com.maru.repository.enrollment;

import com.maru.domain.enrollment.Enrollment;
import com.maru.repository.enrollment.projection.StudentCountByDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.student s
            WHERE e.dojangId = :dojangId
            AND e.division.id = :divisionId
            ORDER BY s.name
            """)
    List<Enrollment> findAllWithStudentByDivisionId(
            @Param("dojangId") String dojangId,
            @Param("divisionId") String divisionId);

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.division d
            WHERE e.dojangId = :dojangId
            AND e.student.id = :studentId
            ORDER BY d.displayOrder
            """)
    List<Enrollment> findAllWithDivisionByStudentId(
            @Param("dojangId") String dojangId,
            @Param("studentId") String studentId);

    boolean existsByDojangIdAndDivisionIdAndStudentId(String dojangId, String divisionId, String studentId);

    Optional<Enrollment> findByDojangIdAndDivisionIdAndStudentId(String dojangId, String divisionId, String studentId);

    @Query("""
            SELECT COUNT(e) FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.division.id = :divisionId
            """)
    int countByDivisionId(@Param("dojangId") String dojangId, @Param("divisionId") String divisionId);

    @Query("""
            SELECT e.student.id FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.division.id = :divisionId
            AND e.student.id IN :studentIds
            """)
    List<String> findAlreadyEnrolledStudentIds(
            @Param("dojangId") String dojangId,
            @Param("divisionId") String divisionId,
            @Param("studentIds") List<String> studentIds);

    @Modifying
    @Query("""
            DELETE FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.division.id = :divisionId
            """)
    void deleteAllByDivisionId(@Param("dojangId") String dojangId, @Param("divisionId") String divisionId);

    @Query("""
            SELECT e.division.id AS divisionId, COUNT(e) AS studentCount
            FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.division.id IN :divisionIds
            GROUP BY e.division.id
            """)
    List<StudentCountByDivision> countStudentsByDivisionIds(
            @Param("dojangId") String dojangId,
            @Param("divisionIds") List<String> divisionIds);

    @Query("""
            SELECT DISTINCT e.student.id FROM Enrollment e
            WHERE e.dojangId = :dojangId
            AND e.student.id IN :studentIds
            """)
    List<String> findEnrolledStudentIds(
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds);
}
