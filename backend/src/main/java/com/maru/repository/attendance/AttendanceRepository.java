package com.maru.repository.attendance;

import com.maru.domain.attendance.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.attendanceDate = :date
        """)
    List<Attendance> findByTenantIdAndDojangIdAndAttendanceDate(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("date") LocalDate date);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.student.id = :studentId
          AND a.attendanceDate BETWEEN :startDate AND :endDate
        """)
    List<Attendance> findByTenantIdAndStudentIdAndAttendanceDateBetween(
            @Param("tenantId") Long tenantId,
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.id = :id
          AND a.dojangId = :dojangId
        """)
    Optional<Attendance> findByTenantIdAndIdAndDojangId(
            @Param("tenantId") Long tenantId,
            @Param("id") Long id,
            @Param("dojangId") Long dojangId);

    @Query("""
        SELECT a.student.id FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.student.id IN :studentIds
          AND a.attendanceDate = :date
        """)
    List<Long> findStudentIdsWithAttendanceToday(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("studentIds") List<Long> studentIds,
            @Param("date") LocalDate date);

    @Query("""
        SELECT a.status, COUNT(a)
        FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND YEAR(a.attendanceDate) = :year
          AND MONTH(a.attendanceDate) = :month
        GROUP BY a.status
        """)
    List<Object[]> countByStatusForMonth(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("year") int year,
            @Param("month") int month);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.attendanceDate BETWEEN :startDate AND :endDate
        """)
    List<Attendance> findByTenantIdAndDojangIdAndAttendanceDateBetween(
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
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
            @Param("tenantId") Long tenantId,
            @Param("dojangId") Long dojangId,
            @Param("ids") List<Long> ids);
}
