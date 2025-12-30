package com.maru.repository.student;

import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
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
          AND s.dojang.id = :dojangId
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
        WHERE s.tenantId = :tenantId
          AND s.dojang.id = :dojangId
          AND s.status = 'ACTIVE'
          AND s.deletedAt IS NULL
          AND NOT EXISTS (
              SELECT 1 FROM Attendance a
              WHERE a.student.id = s.id AND a.attendanceDate = :date
          )
        """)
    List<Student> findActiveStudentsWithoutAttendance(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("date") LocalDate date);

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
        SELECT s FROM Student s
        JOIN FETCH s.dojang d
        WHERE s.status = 'ACTIVE'
          AND s.deletedAt IS NULL
          AND d.deletedAt IS NULL
          AND d.isActive = true
          AND NOT EXISTS (
              SELECT 1 FROM Attendance a
              WHERE a.student.id = s.id AND a.attendanceDate = :date
          )
        """)
    List<Student> findAllActiveStudentsWithoutAttendance(@Param("date") LocalDate date);
}
