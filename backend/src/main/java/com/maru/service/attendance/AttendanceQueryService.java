package com.maru.service.attendance;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.controller.attendance.dto.AttendanceInfo;
import com.maru.controller.attendance.dto.AttendanceRes;
import com.maru.controller.attendance.dto.RangeAttendanceRes;
import com.maru.controller.attendance.dto.StudentAttendanceRowRes;
import com.maru.domain.attendance.exception.AttendanceErrorCode;
import com.maru.domain.student.StudentStatus;
import com.maru.repository.attendance.AttendanceRepository;
import com.maru.repository.attendance.view.AttendanceStudentView;
import com.maru.repository.attendance.view.RangeAttendanceView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ValidateDojangAccess
public class AttendanceQueryService {

    private final AttendanceRepository attendanceRepository;

    /**
     * 출석 단건 조회
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param attendanceId 출석 기록 ID
     * @return 출석 정보
     * @throws BusinessException NOT_FOUND - 출석 기록을 찾을 수 없는 경우
     */
    public AttendanceRes getAttendance(String tenantId, String dojangId, String attendanceId) {
        AttendanceStudentView view = attendanceRepository.findDetailById(tenantId, dojangId, attendanceId)
                .orElseThrow(() -> new BusinessException(AttendanceErrorCode.NOT_FOUND));
        return toAttendanceRes(view);
    }

    /**
     * 출석 이력 조회
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 출석 이력 목록
     */
    public List<AttendanceRes> getHistory(String tenantId, String dojangId, String studentId,
                                          LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findAllWithStudentByDateRange(tenantId, dojangId, studentId, startDate, endDate)
                .stream()
                .map(this::toAttendanceRes)
                .toList();
    }

    /**
     * 출석 기록 복수 조회
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param attendanceIds 출석 기록 ID 목록
     * @return 출석 정보 목록
     */
    public List<AttendanceRes> getAttendances(String tenantId, String dojangId, List<String> attendanceIds) {
        if (attendanceIds.isEmpty()) {
            return List.of();
        }
        return attendanceRepository.findAllDetailByIds(tenantId, dojangId, attendanceIds)
                .stream()
                .map(this::toAttendanceRes)
                .toList();
    }

    /**
     * 기간별 출석 현황 조회
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param filteredStudentIds 필터링된 원생 ID 목록 (null이면 전체)
     * @return 기간별 출석 현황
     */
    public RangeAttendanceRes getAttendanceRange(String tenantId, String dojangId,
                                                  LocalDate startDate, LocalDate endDate,
                                                  List<String> filteredStudentIds) {
        List<RangeAttendanceView> views = fetchRangeViews(tenantId, dojangId, startDate, endDate, filteredStudentIds);

        if (views.isEmpty()) {
            return RangeAttendanceRes.empty(startDate, endDate);
        }

        return toRangeAttendanceRes(views, startDate, endDate);
    }

    private List<RangeAttendanceView> fetchRangeViews(String tenantId, String dojangId,
                                                       LocalDate startDate, LocalDate endDate,
                                                       List<String> filteredStudentIds) {
        if (filteredStudentIds != null) {
            if (filteredStudentIds.isEmpty()) {
                return List.of();
            }
            return attendanceRepository.findRangeWithStudentByStudentIds(
                    tenantId, dojangId, filteredStudentIds, startDate, endDate, StudentStatus.WITHDRAWN);
        }
        return attendanceRepository.findRangeWithStudent(
                tenantId, dojangId, startDate, endDate, StudentStatus.WITHDRAWN);
    }

    private RangeAttendanceRes toRangeAttendanceRes(List<RangeAttendanceView> views,
                                                     LocalDate startDate, LocalDate endDate) {
        Map<String, List<RangeAttendanceView>> groupedByStudent = views.stream()
                .collect(Collectors.groupingBy(
                        RangeAttendanceView::getStudentId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<StudentAttendanceRowRes> studentRows = groupedByStudent.entrySet().stream()
                .map(entry -> toStudentAttendanceRowRes(entry.getValue()))
                .toList();

        return RangeAttendanceRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalStudents(studentRows.size())
                .students(studentRows)
                .build();
    }

    private StudentAttendanceRowRes toStudentAttendanceRowRes(List<RangeAttendanceView> studentViews) {
        RangeAttendanceView first = studentViews.get(0);

        Map<LocalDate, AttendanceInfo> attendances = studentViews.stream()
                .filter(v -> v.getAttendanceId() != null)
                .collect(Collectors.toMap(
                        RangeAttendanceView::getAttendanceDate,
                        this::toAttendanceInfo,
                        (a, b) -> a));

        return StudentAttendanceRowRes.builder()
                .id(first.getStudentId())
                .name(first.getStudentName())
                .photoUrl(first.getStudentPhotoUrl())
                .className(null)
                .attendances(attendances)
                .build();
    }

    private AttendanceInfo toAttendanceInfo(RangeAttendanceView view) {
        return AttendanceInfo.builder()
                .id(view.getAttendanceId())
                .status(view.getStatus())
                .checkinAt(view.getCheckinAt())
                .checkoutAt(view.getCheckoutAt())
                .build();
    }

    private AttendanceRes toAttendanceRes(AttendanceStudentView view) {
        return AttendanceRes.builder()
                .id(view.getId())
                .studentId(view.getStudentId())
                .studentName(view.getStudentName())
                .status(view.getStatus())
                .method(view.getMethod())
                .attendanceDate(view.getAttendanceDate())
                .checkinAt(view.getCheckinAt())
                .checkoutAt(view.getCheckoutAt())
                .note(view.getNote())
                .createdAt(view.getCreatedAt())
                .build();
    }
}
