package com.maru.repository.attendance;

import com.maru.domain.attendance.Attendance;
import com.maru.domain.student.StudentStatus;
import com.maru.repository.attendance.view.AttendanceStudentView;
import com.maru.repository.attendance.view.CurrentAttendanceCountView;
import com.maru.repository.attendance.view.NotificationTargetView;
import com.maru.repository.attendance.view.RangeAttendanceView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {

    @Query("""
        SELECT a.id as id, a.studentId as studentId, s.name as studentName,
               a.status as status, a.method as method, a.attendanceDate as attendanceDate,
               a.checkinAt as checkinAt, a.checkoutAt as checkoutAt,
               a.note as note, a.createdAt as createdAt
        FROM Attendance a
        JOIN Student s ON a.studentId = s.id
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.id = :id
        """)
    Optional<AttendanceStudentView> findDetailById(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("id") String id);

    @Query("""
        SELECT a.id as id, a.studentId as studentId, s.name as studentName,
               a.status as status, a.method as method, a.attendanceDate as attendanceDate,
               a.checkinAt as checkinAt, a.checkoutAt as checkoutAt,
               a.note as note, a.createdAt as createdAt
        FROM Attendance a
        JOIN Student s ON a.studentId = s.id
        
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.studentId = :studentId
          AND a.attendanceDate BETWEEN :startDate AND :endDate
        ORDER BY a.attendanceDate DESC
        """)
    List<AttendanceStudentView> findAllWithStudentByDateRange(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentId") String studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT a.id as id, a.studentId as studentId, s.name as studentName,
               a.status as status, a.method as method, a.attendanceDate as attendanceDate,
               a.checkinAt as checkinAt, a.checkoutAt as checkoutAt,
               a.note as note, a.createdAt as createdAt
        FROM Attendance a
        JOIN Student s ON a.studentId = s.id
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.id IN :ids
        """)
    List<AttendanceStudentView> findAllDetailByIds(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("ids") List<String> ids);

    @Query("""
        SELECT a FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.studentId = :studentId
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
        SELECT a.studentId FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.studentId IN :studentIds
          AND a.attendanceDate = :date
        """)
    List<String> findStudentIdsWithAttendanceToday(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("date") LocalDate date);

    @Query("""
        SELECT a FROM Attendance a
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
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.studentId IN :studentIds
          AND a.attendanceDate BETWEEN :startDate AND :endDate
        """)
    List<Attendance> findByDojangIdAndDateRangeAndStudentIds(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT s.id as studentId, s.name as studentName, s.photoUrl as studentPhotoUrl,
               a.id as attendanceId, a.status as status, a.attendanceDate as attendanceDate,
               a.checkinAt as checkinAt, a.checkoutAt as checkoutAt
        FROM Student s
        LEFT JOIN Attendance a ON s.id = a.studentId
            AND a.attendanceDate BETWEEN :startDate AND :endDate
        WHERE s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.status != :excludeStatus
        ORDER BY s.name, a.attendanceDate
        """)
    List<RangeAttendanceView> findRangeWithStudent(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT s.id as studentId, s.name as studentName, s.photoUrl as studentPhotoUrl,
               a.id as attendanceId, a.status as status, a.attendanceDate as attendanceDate,
               a.checkinAt as checkinAt, a.checkoutAt as checkoutAt
        FROM Student s
        LEFT JOIN Attendance a ON s.id = a.studentId
            AND a.attendanceDate BETWEEN :startDate AND :endDate
        WHERE s.tenantId = :tenantId
          AND s.dojangId = :dojangId
          AND s.status != :excludeStatus
          AND s.id IN :studentIds
        ORDER BY s.name, a.attendanceDate
        """)
    List<RangeAttendanceView> findRangeWithStudentByStudentIds(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("studentIds") List<String> studentIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeStatus") StudentStatus excludeStatus);

    @Query("""
        SELECT a.id as id, a.tenantId as tenantId, a.dojangId as dojangId,
               a.studentId as studentId, s.name as studentName, d.name as dojangName
        FROM Attendance a
        JOIN Student s ON a.studentId = s.id
        JOIN Dojang d ON a.dojangId = d.id
        WHERE a.id = :id
        """)
    Optional<NotificationTargetView> findNotificationTarget(@Param("id") String id);

    @Query("""
        SELECT COUNT(a) as count
        FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.attendanceDate = :date
          AND a.checkinAt IS NOT NULL
          AND a.checkoutAt IS NULL
        """)
    CurrentAttendanceCountView countCurrentlyAttending(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("date") LocalDate date);

    @Query("""
        SELECT COUNT(a) as count
        FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.attendanceDate = :date
          AND a.status = 'PRESENT'
        """)
    long countTodayPresent(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("date") LocalDate date);

    @Query("""
        SELECT COUNT(a) as count
        FROM Attendance a
        WHERE a.tenantId = :tenantId
          AND a.dojangId = :dojangId
          AND a.attendanceDate BETWEEN :startDate AND :endDate
          AND a.status = 'PRESENT'
        """)
    long countMonthlyPresent(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
