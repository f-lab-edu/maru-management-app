package com.maru.repository.attendance;

import com.maru.domain.attendance.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.student.id = :studentId
          AND a.attendanceDate BETWEEN :startDate AND :endDate
        """)
    List<Attendance> findByTenantIdAndDojangIdAndStudentIdAndAttendanceDateBetween(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentId") String studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.id = :id
          AND a.dojangId = :dojangId
        """)
    Optional<Attendance> findByTenantIdAndIdAndDojangId(
            @Param("tenantId") String tenantId,
            @Param("id") String id,
            @Param("dojangId") String dojangId);

    @Query("""
        SELECT a.student.id FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.student.id IN :studentIds
          AND a.attendanceDate = :date
        """)
    List<String> findStudentIdsWithAttendanceToday(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("date") LocalDate date);

    @Query("""
        SELECT a FROM Attendance a
        JOIN FETCH a.student
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.attendanceDate BETWEEN :startDate AND :endDate
        """)
    List<Attendance> findByTenantIdAndDojangIdAndAttendanceDateBetween(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.id IN :ids
          AND a.checkoutAt IS NULL
        """)
    List<Attendance> findByTenantIdAndDojangIdAndIdInAndCheckoutAtIsNull(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("ids") List<String> ids);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.id IN :ids
        """)
    List<Attendance> findByTenantIdAndDojangIdAndIdIn(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("ids") List<String> ids);

    @Query("""
        SELECT a FROM Attendance a
        JOIN FETCH a.student
        WHERE a.id = :id
        """)
    Optional<Attendance> findByIdWithStudent(@Param("id") String id);

    @Query("""
        SELECT a FROM Attendance a
        JOIN FETCH a.student
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.student.id IN :studentIds
          AND a.attendanceDate BETWEEN :startDate AND :endDate
        """)
    List<Attendance> findByDojangIdAndDateRangeAndStudentIds(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
