package com.maru.repository.attendance;

import com.maru.domain.attendance.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByDojangIdAndAttendanceDate(Long dojangId, LocalDate date);

    List<Attendance> findByStudentIdAndAttendanceDateBetween(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate);

    Optional<Attendance> findByIdAndDojangId(Long id, Long dojangId);

    boolean existsByDojangIdAndStudentIdAndAttendanceDate(
            Long dojangId,
            Long studentId,
            LocalDate date);

    @Query("""
        SELECT a.student.id FROM Attendance a
        WHERE a.dojangId = :dojangId
          AND a.student.id IN :studentIds
          AND a.attendanceDate = :date
        """)
    List<Long> findStudentIdsWithAttendanceToday(
            @Param("dojangId") Long dojangId,
            @Param("studentIds") List<Long> studentIds,
            @Param("date") LocalDate date);

    @Query("""
        SELECT a.status, COUNT(a)
        FROM Attendance a
        WHERE a.dojangId = :dojangId
          AND YEAR(a.attendanceDate) = :year
          AND MONTH(a.attendanceDate) = :month
        GROUP BY a.status
        """)
    List<Object[]> countByStatusForMonth(
            @Param("dojangId") Long dojangId,
            @Param("year") int year,
            @Param("month") int month);

    List<Attendance> findByDojangIdAndAttendanceDateBetween(
            Long dojangId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
        SELECT a.status, COUNT(a)
        FROM Attendance a
        WHERE a.dojangId = :dojangId
          AND a.attendanceDate = :date
        GROUP BY a.status
        """)
    List<Object[]> countByStatusForDate(
            @Param("dojangId") Long dojangId,
            @Param("date") LocalDate date);
}
