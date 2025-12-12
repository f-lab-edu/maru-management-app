package com.maru.repository.student;

import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
        SELECT s FROM Student s
        WHERE s.tenantId = :tenantId
          AND s.dojang.id = :dojangId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        ORDER BY s.enrolledAt DESC
        """)
    List<Student> findActiveStudents(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s FROM Student s
        WHERE s.id = :id
          AND s.tenantId = :tenantId
          AND s.status != :excludeStatus
          AND s.deletedAt IS NULL
        """)
    Optional<Student> findActiveById(
            @Param("id") Long id,
            @Param("tenantId") Long tenantId,
            @Param("excludeStatus") StudentStatus excludeStatus);

    Optional<Student> findByDojangIdAndNameAndBirth(Long dojangId, String name, LocalDate birth);
}
