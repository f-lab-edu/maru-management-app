package com.maru.service.dashboard;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.controller.dashboard.dto.DashboardNotificationRes;
import com.maru.controller.dashboard.dto.DashboardNotificationRes.NotificationItem;
import com.maru.controller.dashboard.dto.DashboardSummaryRes;
import com.maru.controller.dashboard.dto.RecentStudentRes;
import com.maru.controller.dashboard.dto.RecentStudentRes.StudentItem;
import com.maru.repository.attendance.AttendanceRepository;
import com.maru.repository.attendance.view.CurrentAttendanceCountView;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.repository.employment.view.RecentApprovalView;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.view.InvoiceStatisticsView;
import com.maru.repository.invoice.view.RecentPaymentView;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.student.view.RecentStudentView;
import com.maru.repository.student.view.StudentCountView;
import com.maru.security.TenantContextHolder;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ValidateDojangAccess
public class DashboardQueryService {

    private static final int RECENT_DAYS = 7;

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final InvoiceRepository invoiceRepository;
    private final EmploymentRepository employmentRepository;

    public DashboardSummaryRes getSummary(String dojangId) {
        String tenantId = TenantContextHolder.getTenantId();

        int currentlyAttending = countCurrentlyAttending(tenantId, dojangId);
        StudentStats studentStats = calculateStudentStats(tenantId, dojangId);
        AttendanceStats attendanceStats = calculateAttendanceStats(tenantId, dojangId, studentStats);
        RevenueStats revenueStats = calculateRevenueStats(tenantId, dojangId);

        return buildSummaryResponse(currentlyAttending, studentStats, attendanceStats, revenueStats);
    }

    public DashboardNotificationRes getNotifications(String dojangId) {
        String tenantId = TenantContextHolder.getTenantId();

        List<NotificationItem> items = collectNotificationItems(tenantId, dojangId);

        return DashboardNotificationRes.builder()
                .items(items)
                .build();
    }

    public RecentStudentRes getRecentStudents(String dojangId) {
        String tenantId = TenantContextHolder.getTenantId();

        List<StudentItem> students = findRecentStudents(tenantId, dojangId);

        return RecentStudentRes.builder()
                .students(students)
                .build();
    }

    // ==================== getSummary 상세 구현 ====================

    private int countCurrentlyAttending(String tenantId, String dojangId) {
        LocalDate today = LocalDate.now();
        CurrentAttendanceCountView view = attendanceRepository.countCurrentlyAttending(tenantId, dojangId, today);
        return (int) view.getCount();
    }

    private StudentStats calculateStudentStats(String tenantId, String dojangId) {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);

        StudentCountView currentCount = studentRepository.countByStatus(tenantId, dojangId);
        long lastMonthCount = studentRepository.countAsOfDate(
                tenantId, dojangId, lastMonth.atEndOfMonth().atTime(23, 59, 59));

        int diff = (int) (currentCount.getTotalCount() - lastMonthCount);

        return new StudentStats(
                (int) currentCount.getTotalCount(),
                (int) currentCount.getActiveCount(),
                (int) currentCount.getPausedCount(),
                diff
        );
    }

    private AttendanceStats calculateAttendanceStats(String tenantId, String dojangId, StudentStats studentStats) {
        LocalDate today = LocalDate.now();
        YearMonth lastMonth = YearMonth.now().minusMonths(1);

        long todayPresent = attendanceRepository.countTodayPresent(tenantId, dojangId, today);
        long lastMonthPresent = attendanceRepository.countMonthlyPresent(
                tenantId, dojangId, lastMonth.atDay(1), lastMonth.atEndOfMonth());

        double todayRate = calculateRate(todayPresent, studentStats.activeCount());
        double lastMonthRate = calculateMonthlyRate(lastMonthPresent, studentStats.totalCount(), lastMonth.lengthOfMonth());
        double rateDiff = todayRate - lastMonthRate;

        return new AttendanceStats(
                roundToOneDecimal(todayRate),
                roundToOneDecimal(rateDiff)
        );
    }

    private RevenueStats calculateRevenueStats(String tenantId, String dojangId) {
        YearMonth currentMonth = YearMonth.now();
        InvoiceStatisticsView stats = invoiceRepository.getStatistics(tenantId, dojangId, currentMonth);

        return new RevenueStats(
                getOrZero(stats, InvoiceStatisticsView::getTotalPaidAmount),
                getOrZero(stats, InvoiceStatisticsView::getTotalAmount)
        );
    }

    private DashboardSummaryRes buildSummaryResponse(int currentlyAttending,
                                                      StudentStats student,
                                                      AttendanceStats attendance,
                                                      RevenueStats revenue) {
        return DashboardSummaryRes.builder()
                .currentlyAttendingCount(currentlyAttending)
                .totalStudents(student.totalCount())
                .studentsDiff(student.diff())
                .attendanceRate(attendance.rate())
                .attendanceRateDiff(attendance.rateDiff())
                .monthlyRevenue(revenue.paidAmount())
                .revenueTarget(revenue.targetAmount())
                .activeStudents(student.activeCount())
                .pausedStudents(student.pausedCount())
                .build();
    }

    // ==================== getNotifications 상세 구현 ====================

    private List<NotificationItem> collectNotificationItems(String tenantId, String dojangId) {
        LocalDateTime since = LocalDateTime.now().minusDays(RECENT_DAYS);
        LocalDate sinceDate = LocalDate.now().minusDays(RECENT_DAYS);

        List<RecentPaymentView> payments = invoiceRepository.findRecentPayments(tenantId, dojangId, since);
        List<RecentApprovalView> approvals = employmentRepository.findRecentApprovals(tenantId, dojangId, since);
        List<RecentStudentView> students = studentRepository.findRecentEnrolled(tenantId, dojangId, sinceDate);

        return Stream.of(
                        payments.stream().map(this::toPaymentNotification),
                        approvals.stream().map(this::toApprovalNotification),
                        students.stream().map(this::toStudentNotification))
                .flatMap(s -> s)
                .sorted(Comparator.comparing(NotificationItem::occurredAt).reversed())
                .toList();
    }

    private NotificationItem toPaymentNotification(RecentPaymentView payment) {
        return NotificationItem.builder()
                .type("PAYMENT")
                .title(payment.getStudentName() + " 원생 수납 완료")
                .description("수련비 결제가 완료되었습니다.")
                .occurredAt(payment.getPaidAt())
                .build();
    }

    private NotificationItem toApprovalNotification(RecentApprovalView approval) {
        return NotificationItem.builder()
                .type("INSTRUCTOR")
                .title(approval.getUserName() + " 사범 승인")
                .description("새로운 사범이 승인되었습니다.")
                .occurredAt(approval.getApprovedAt())
                .build();
    }

    private NotificationItem toStudentNotification(RecentStudentView student) {
        return NotificationItem.builder()
                .type("STUDENT")
                .title(student.getName() + " 원생 입관")
                .description("새로운 원생이 입관하였습니다.")
                .occurredAt(student.getEnrolledAt().atStartOfDay())
                .build();
    }

    // ==================== getRecentStudents 상세 구현 ====================

    private List<StudentItem> findRecentStudents(String tenantId, String dojangId) {
        LocalDate since = LocalDate.now().minusDays(RECENT_DAYS);
        List<RecentStudentView> views = studentRepository.findRecentEnrolled(tenantId, dojangId, since);

        return views.stream()
                .map(this::toStudentItem)
                .toList();
    }

    private StudentItem toStudentItem(RecentStudentView view) {
        return StudentItem.builder()
                .id(view.getId())
                .name(view.getName())
                .enrolledAt(view.getEnrolledAt())
                .status(view.getStatus())
                .photoUrl(view.getPhotoUrl())
                .build();
    }

    // ==================== 유틸리티 ====================

    private double calculateRate(long count, long total) {
        if (total == 0) {
            return 0.0;
        }
        return (double) count / total * 100;
    }

    private double calculateMonthlyRate(long monthlyCount, long studentCount, int daysInMonth) {
        if (studentCount == 0 || daysInMonth == 0) {
            return 0.0;
        }
        long expectedAttendance = studentCount * daysInMonth;
        return (double) monthlyCount / expectedAttendance * 100;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private BigDecimal getOrZero(InvoiceStatisticsView stats,
                                  java.util.function.Function<InvoiceStatisticsView, BigDecimal> getter) {
        if (stats == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = getter.apply(stats);
        return value != null ? value : BigDecimal.ZERO;
    }

    // ==================== 내부 DTO ====================

    private record StudentStats(int totalCount, int activeCount, int pausedCount, int diff) {}
    private record AttendanceStats(double rate, double rateDiff) {}
    private record RevenueStats(BigDecimal paidAmount, BigDecimal targetAmount) {}
}
