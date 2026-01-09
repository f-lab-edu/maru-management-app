package com.maru.repository.student;

import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.repository.student.view.StudentDetailView;
import com.maru.repository.student.view.StudentMinimalView;
import com.maru.repository.student.view.StudentSummaryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {

    @Query("""
        SELECT s FROM Student s
        WHERE s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        ORDER BY s.enrolledAt DESC
        """)
    List<Student> findActiveStudents(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s FROM Student s
        WHERE s.id = :id
          AND s.tenantId = :tenantId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    Optional<Student> findActiveById(
            @Param("id") String id,
            @Param("tenantId") String tenantId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    Optional<Student> findByDojangIdAndNameAndBirth(String dojangId, String name, LocalDate birth);

    @Query("""
        SELECT s FROM Student s
        WHERE s.id IN :ids
          AND s.tenantId = :tenantId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    List<Student> findAllActiveByIds(
            @Param("ids") List<String> ids,
            @Param("tenantId") String tenantId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s FROM Student s, Dojang d
        WHERE s.dojangId = d.id
          AND s.status = 'ACTIVE'
          AND s.deletedAt IS NULL
          AND d.deletedAt IS NULL
          AND d.isActive = true
          AND NOT EXISTS (
              SELECT 1 FROM Attendance a
              WHERE a.studentId = s.id AND a.attendanceDate = :date
          )
        """)
    List<Student> findAllActiveStudentsWithoutAttendance(@Param("date") LocalDate date);

    @Query("""
        SELECT s FROM Student s
        WHERE s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.id IN :studentIds
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        ORDER BY s.enrolledAt DESC
        """)
    List<Student> findActiveStudentsByIds(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Student s
        WHERE s.id = :id
          AND s.tenantId = :tenantId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    boolean existsActiveById(
            @Param("id") String id,
            @Param("tenantId") String tenantId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s.id FROM Student s
        WHERE s.id IN :ids
          AND s.tenantId = :tenantId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    List<String> findActiveStudentIds(
            @Param("ids") List<String> ids,
            @Param("tenantId") String tenantId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s.id as id, s.name as name, s.birth as birth,
               s.photoUrl as photoUrl, s.phone as phone,
               s.enrolledAt as enrolledAt, s.status as status
        FROM Student s
        WHERE s.id = :id
          AND s.tenantId = :tenantId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    Optional<StudentDetailView> findDetailById(
            @Param("id") String id,
            @Param("tenantId") String tenantId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s.id as id, s.name as name, s.birth as birth,
               s.photoUrl as photoUrl, s.enrolledAt as enrolledAt, s.status as status,
               (SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.studentId = s.id AND e.dojangId = :dojangId) as hasEnrollment
        FROM Student s
        WHERE s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        ORDER BY s.enrolledAt DESC
        """)
    List<StudentSummaryView> findAllWithEnrollmentStatus(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s.id as id, s.name as name, s.birth as birth,
               s.photoUrl as photoUrl, s.enrolledAt as enrolledAt, s.status as status,
               (SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.studentId = s.id AND e.dojangId = :dojangId) as hasEnrollment
        FROM Student s
        WHERE s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.id IN :studentIds
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        ORDER BY s.enrolledAt DESC
        """)
    List<StudentSummaryView> findAllByIdsWithEnrollmentStatus(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Student s
        WHERE s.id = :id
          AND s.dojangId = :dojangId
          AND s.deletedAt IS NULL
        """)
    boolean existsByIdAndDojangId(
            @Param("id") String id,
            @Param("dojangId") String dojangId);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Student s
        WHERE s.id = :id
          AND s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    boolean existsActiveByIdAndDojangId(
            @Param("id") String id,
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s.id FROM Student s
        WHERE s.id IN :ids
          AND s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    List<String> findActiveStudentIdsByIdsAndDojang(
            @Param("ids") List<String> ids,
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s.id FROM Student s
        WHERE s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    List<String> findActiveStudentIdsByDojang(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s.id as id, s.tenantId as tenantId, s.dojangId as dojangId, s.name as name
        FROM Student s, Dojang d
        WHERE s.dojangId = d.id
          AND s.status = 'ACTIVE'
          AND s.deletedAt IS NULL
          AND d.deletedAt IS NULL
          AND d.isActive = true
          AND NOT EXISTS (
              SELECT 1 FROM Attendance a
              WHERE a.studentId = s.id AND a.attendanceDate = :date
          )
        """)
    List<StudentMinimalView> findAllActiveStudentsWithoutAttendanceMinimal(@Param("date") LocalDate date);

    @Query("""
        SELECT s.id as id, s.tenantId as tenantId, s.dojangId as dojangId, s.name as name
        FROM Student s
        WHERE s.id = :id
          AND s.dojangId = :dojangId
          AND s.deletedAt IS NULL
        """)
    Optional<StudentMinimalView> findMinimalByIdAndDojangId(
            @Param("id") String id,
            @Param("dojangId") String dojangId);
}
