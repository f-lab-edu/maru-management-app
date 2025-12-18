package com.maru.service.attendance;

import com.maru.common.exception.BusinessException;
import com.maru.controller.attendance.dto.*;
import com.maru.domain.attendance.Attendance;
import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;
import com.maru.domain.attendance.event.AttendanceCheckedEvent;
import com.maru.domain.attendance.exception.AttendanceErrorCode;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.attendance.AttendanceRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final int MAX_DATE_RANGE_DAYS = 31;
    private static final int MAX_RETROACTIVE_DAYS = 30;
    private static final LocalTime DEFAULT_CHECKIN_TIME = LocalTime.of(9, 0);

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 단일 원생 출석 기록 생성
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param method 체크 방법
     * @param status 출석 상태 (null이면 PRESENT)
     * @param date 출석 날짜 (null이면 오늘, 과거 30일 이내)
     * @param checkinAt 체크인 시각 (null이면 현재 시각 또는 date의 기본 시각, 결석/병결/공결은 무시)
     * @param note 비고 (선택)
     * @return 출석 기록
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     * @throws BusinessException ATTENDANCE_RETROACTIVE_LIMIT_EXCEEDED - 소급 입력 30일 초과
     * @throws BusinessException ATTENDANCE_DUPLICATE - 이미 출석 체크됨
     */
    @Transactional
    public AttendanceRes checkIn(Long dojangId, Long studentId, CheckMethod method, AttendanceStatus status,
                                  LocalDate date, LocalDateTime checkinAt, String note) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);
        Student student = findStudentInDojang(studentId, dojangId, tenantId);
        LocalDate targetDate = resolveTargetDate(date);

        AttendanceStatus targetStatus = status != null ? status : AttendanceStatus.PRESENT;
        LocalDateTime targetCheckinAt = targetStatus == AttendanceStatus.PRESENT
                ? resolveCheckinAt(targetDate, checkinAt)
                : targetDate.atTime(DEFAULT_CHECKIN_TIME);

        Attendance attendance = saveAttendanceWithDuplicateCheck(student, method, targetStatus, targetCheckinAt, note);

        publishCheckedEvent(attendance, tenantId, true);

        log.info("출석 기록 생성 - attendanceId: {}, studentId: {}, status: {}, dojangId: {}",
                attendance.getId(), studentId, targetStatus, dojangId);
        return AttendanceRes.from(attendance);
    }

    /**
     * 일괄 출석 체크
     *
     * @param dojangId 도장 ID
     * @param studentIds 원생 ID 목록
     * @param method 체크 방법
     * @return 성공/실패 결과
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     */
    @Transactional
    public BulkCheckRes bulkCheckIn(Long dojangId, List<Long> studentIds, CheckMethod method) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);

        List<Student> validStudents = new ArrayList<>();
        List<BulkCheckFailureRes> failures = new ArrayList<>();
        validateAndCollectStudentsForCheckIn(tenantId, dojangId, studentIds, validStudents, failures);

        List<Attendance> attendances = createAndSaveBulkAttendances(validStudents, method, dojangId);

        publishBulkCheckedEvents(attendances, tenantId, true);

        log.info("일괄 출석 체크 - dojangId: {}, success: {}, failure: {}", dojangId, attendances.size(), failures.size());
        return buildBulkCheckRes(attendances, failures);
    }

    /**
     * 퇴관 처리
     *
     * @param dojangId 도장 ID
     * @param attendanceId 출석 기록 ID
     * @return 업데이트된 출석 기록
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     * @throws BusinessException ATTENDANCE_NOT_FOUND - 출석 기록을 찾을 수 없음
     * @throws BusinessException ATTENDANCE_ALREADY_CHECKOUT - 이미 퇴관 처리됨
     */
    @Transactional
    public AttendanceRes checkOut(Long dojangId, Long attendanceId) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);
        Attendance attendance = findAttendanceInDojang(tenantId, attendanceId, dojangId);

        attendance.checkOut(LocalDateTime.now());

        publishCheckedEvent(attendance, tenantId, false);

        log.info("퇴관 처리 - attendanceId: {}, dojangId: {}", attendanceId, dojangId);
        return AttendanceRes.from(attendance);
    }

    /**
     * 퇴관 취소
     *
     * @param dojangId 도장 ID
     * @param attendanceId 출석 기록 ID
     * @return 업데이트된 출석 기록
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     * @throws BusinessException ATTENDANCE_NOT_FOUND - 출석 기록을 찾을 수 없음
     * @throws BusinessException ATTENDANCE_NOT_CHECKOUT - 퇴관 기록이 없음
     */
    @Transactional
    public AttendanceRes cancelCheckout(Long dojangId, Long attendanceId) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);
        Attendance attendance = findAttendanceInDojang(tenantId, attendanceId, dojangId);

        attendance.cancelCheckout();

        log.info("퇴관 취소 - attendanceId: {}, dojangId: {}", attendanceId, dojangId);
        return AttendanceRes.from(attendance);
    }

    /**
     * 일괄 퇴관 처리
     *
     * @param dojangId 도장 ID
     * @param attendanceIds 출석 기록 ID 목록
     * @return 성공/실패 결과
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     */
    @Transactional
    public BulkCheckRes bulkCheckOut(Long dojangId, List<Long> attendanceIds) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);

        List<Attendance> attendances = findCheckableAttendances(tenantId, dojangId, attendanceIds);
        List<BulkCheckFailureRes> failures = buildNotFoundFailures(attendanceIds, attendances);

        processCheckOut(attendances);

        publishBulkCheckedEvents(attendances, tenantId, false);

        log.info("일괄 퇴관 처리 - dojangId: {}, success: {}, failure: {}", dojangId, attendances.size(), failures.size());
        return buildBulkCheckRes(attendances, failures);
    }

    /**
     * 출석 상태 변경
     *
     * @param dojangId 도장 ID
     * @param attendanceId 출석 기록 ID
     * @param status 새로운 상태
     * @param note 비고 (선택)
     * @return 업데이트된 출석 기록
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     * @throws BusinessException ATTENDANCE_NOT_FOUND - 출석 기록을 찾을 수 없음
     * @throws BusinessException ATTENDANCE_SAME_STATUS - 이미 동일한 상태
     */
    @Transactional
    public AttendanceRes changeStatus(Long dojangId, Long attendanceId, AttendanceStatus status, String note) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);
        Attendance attendance = findAttendanceInDojang(tenantId, attendanceId, dojangId);

        attendance.changeStatus(status, note);

        log.info("출석 상태 변경 - attendanceId: {}, status: {}, dojangId: {}", attendanceId, status, dojangId);
        return AttendanceRes.from(attendance);
    }

    /**
     * 출석 시간 변경
     *
     * @param dojangId 도장 ID
     * @param attendanceId 출석 기록 ID
     * @param checkinAt 새로운 출석 시간 (null이면 변경 안 함)
     * @param checkoutAt 새로운 퇴관 시간 (null이면 변경 안 함)
     * @return 업데이트된 출석 기록
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     * @throws BusinessException ATTENDANCE_NOT_FOUND - 출석 기록을 찾을 수 없음
     * @throws BusinessException ATTENDANCE_CHECKOUT_BEFORE_CHECKIN - 퇴관 시간이 출석 시간보다 이전
     */
    @Transactional
    public AttendanceRes changeTime(Long dojangId, Long attendanceId, LocalDateTime checkinAt, LocalDateTime checkoutAt) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);
        Attendance attendance = findAttendanceInDojang(tenantId, attendanceId, dojangId);

        attendance.changeTime(checkinAt, checkoutAt);

        log.info("출석 시간 변경 - attendanceId: {}, checkinAt: {}, checkoutAt: {}, dojangId: {}",
                attendanceId, checkinAt, checkoutAt, dojangId);
        return AttendanceRes.from(attendance);
    }

    /**
     * 일괄 출석 상태 변경
     *
     * @param dojangId 도장 ID
     * @param attendanceIds 출석 기록 ID 목록
     * @param status 새로운 상태
     * @param note 비고 (선택)
     * @return 성공/실패 결과
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     */
    @Transactional
    public BulkCheckRes bulkChangeStatus(Long dojangId, List<Long> attendanceIds, AttendanceStatus status, String note) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);

        List<Attendance> attendances = attendanceRepository.findByTenantIdAndDojangIdAndIdIn(tenantId, dojangId, attendanceIds);

        List<Attendance> successAttendances = new ArrayList<>();
        List<BulkCheckFailureRes> failures = new ArrayList<>();
        validateAndChangeStatus(attendanceIds, attendances, status, note, successAttendances, failures);

        log.info("일괄 출석 상태 변경 - dojangId: {}, status: {}, success: {}, failure: {}",
                dojangId, status, successAttendances.size(), failures.size());
        return buildBulkCheckRes(successAttendances, failures);
    }

    /**
     * 기간별 출석 현황 조회 (7일/1개월 뷰)
     *
     * @param dojangId 도장 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜 (최대 31일 범위)
     * @return 기간별 출석 현황
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     * @throws BusinessException ATTENDANCE_DATE_RANGE_INVALID - 시작일이 종료일보다 이후
     * @throws BusinessException ATTENDANCE_DATE_RANGE_TOO_LARGE - 조회 기간 31일 초과
     */
    @Transactional(readOnly = true)
    public RangeAttendanceRes getAttendanceRange(Long dojangId, LocalDate startDate, LocalDate endDate) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);
        validateDateRange(startDate, endDate);

        List<Student> students = studentRepository.findActiveStudents(tenantId, dojangId, StudentStatus.WITHDRAWN);
        List<Attendance> attendances = attendanceRepository
                .findByTenantIdAndDojangIdAndAttendanceDateBetween(tenantId, dojangId, startDate, endDate);

        return buildRangeAttendanceRes(students, attendances, startDate, endDate);
    }

    /**
     * 출석 이력 조회
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 출석 이력 목록
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     * @throws BusinessException STUDENT_NOT_FOUND - 원생을 찾을 수 없음
     * @throws BusinessException ATTENDANCE_DATE_RANGE_INVALID - 시작일이 종료일보다 이후
     * @throws BusinessException ATTENDANCE_DATE_RANGE_TOO_LARGE - 조회 기간 31일 초과
     */
    @Transactional(readOnly = true)
    public List<AttendanceRes> getHistory(Long dojangId, Long studentId, LocalDate startDate, LocalDate endDate) {
        Long tenantId = validateDojangAndGetTenantId(dojangId);
        validateDateRange(startDate, endDate);
        findStudentInDojang(studentId, dojangId, tenantId);

        return attendanceRepository
                .findByTenantIdAndDojangIdAndStudentIdAndAttendanceDateBetween(tenantId, dojangId, studentId, startDate, endDate)
                .stream()
                .map(AttendanceRes::from)
                .toList();
    }

    /**
     * 자동 결석 처리
     *
     * @param date 처리할 날짜
     * @return 처리된 결석 건수
     */
    @Transactional
    public int processAutoAbsence(LocalDate date) {
        List<Student> students = studentRepository.findAllActiveStudentsWithoutAttendance(date);

        if (students.isEmpty()) {
            log.info("자동 결석 처리 대상 없음: date={}", date);
            return 0;
        }

        List<Attendance> absences = createAbsenceRecords(students, date);
        attendanceRepository.saveAll(absences);

        log.info("자동 결석 처리 완료: date={}, count={}", date, absences.size());
        return absences.size();
    }

    private Long validateDojangAndGetTenantId(Long dojangId) {
        Long tenantId = TenantContextHolder.getTenantId();
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        if (!dojang.getTenant().getId().equals(tenantId)) {
            throw new BusinessException(DojangErrorCode.UNAUTHORIZED_ACCESS);
        }
        return tenantId;
    }

    private Student findStudentInDojang(Long studentId, Long dojangId, Long tenantId) {
        Student student = studentRepository.findActiveById(studentId, tenantId, StudentStatus.WITHDRAWN)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));

        if (!student.getDojang().getId().equals(dojangId)) {
            throw new BusinessException(StudentErrorCode.NOT_FOUND);
        }
        return student;
    }

    private Attendance findAttendanceInDojang(Long tenantId, Long attendanceId, Long dojangId) {
        return attendanceRepository.findByTenantIdAndIdAndDojangId(tenantId, attendanceId, dojangId)
                .orElseThrow(() -> new BusinessException(AttendanceErrorCode.NOT_FOUND));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(AttendanceErrorCode.DATE_RANGE_INVALID);
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) + 1 > MAX_DATE_RANGE_DAYS) {
            throw new BusinessException(AttendanceErrorCode.DATE_RANGE_TOO_LARGE);
        }
    }

    private LocalDate resolveTargetDate(LocalDate date) {
        if (date == null) return LocalDate.now();

        if (ChronoUnit.DAYS.between(date, LocalDate.now()) > MAX_RETROACTIVE_DAYS) {
            throw new BusinessException(AttendanceErrorCode.RETROACTIVE_LIMIT_EXCEEDED);
        }
        return date;
    }

    private LocalDateTime resolveCheckinAt(LocalDate targetDate, LocalDateTime checkinAt) {
        if (checkinAt != null) return checkinAt;
        return targetDate.equals(LocalDate.now())
                ? LocalDateTime.now()
                : targetDate.atTime(DEFAULT_CHECKIN_TIME);
    }

    private Attendance saveAttendanceWithDuplicateCheck(Student student, CheckMethod method, AttendanceStatus status,
                                                         LocalDateTime checkinAt, String note) {
        Attendance attendance = Attendance.create(student, method, status, checkinAt, note);
        try {
            return attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(AttendanceErrorCode.DUPLICATE);
        }
    }

    private Map<Long, Student> findStudentsAsMap(List<Long> studentIds, Long tenantId) {
        return studentRepository.findAllActiveByIds(studentIds, tenantId, StudentStatus.WITHDRAWN)
                .stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
    }

    private Set<Long> findAlreadyCheckedStudentIds(Long tenantId, Long dojangId, List<Long> studentIds) {
        return new HashSet<>(attendanceRepository.findStudentIdsWithAttendanceToday(
                tenantId, dojangId, studentIds, LocalDate.now()));
    }

    private void validateAndCollectStudent(Long studentId, Long dojangId, Map<Long, Student> studentMap,
                                           Set<Long> alreadyCheckedSet, List<Student> validStudents,
                                           List<BulkCheckFailureRes> failureList) {
        if (alreadyCheckedSet.contains(studentId)) {
            failureList.add(buildFailure(studentId, AttendanceErrorCode.DUPLICATE.getMessage()));
            return;
        }

        Student student = studentMap.get(studentId);
        if (student == null) {
            failureList.add(buildFailure(studentId, StudentErrorCode.NOT_FOUND.getMessage()));
            return;
        }
        if (!student.getDojang().getId().equals(dojangId)) {
            failureList.add(buildFailure(studentId, DojangErrorCode.UNAUTHORIZED_ACCESS.getMessage()));
            return;
        }

        validStudents.add(student);
    }

    private BulkCheckFailureRes buildFailure(Long id, String message) {
        return BulkCheckFailureRes.builder().studentId(id).errorMessage(message).build();
    }

    private RangeAttendanceRes buildRangeAttendanceRes(List<Student> students,
                                                        List<Attendance> attendances,
                                                        LocalDate startDate, LocalDate endDate) {
        Map<Long, Map<LocalDate, Attendance>> attendanceMap = groupAttendancesByStudent(attendances);

        List<StudentAttendanceRowRes> studentRows = students.stream()
                .map(s -> buildStudentRow(s, attendanceMap.getOrDefault(s.getId(), Map.of())))
                .toList();

        return RangeAttendanceRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalStudents(students.size())
                .students(studentRows)
                .build();
    }

    private Map<Long, Map<LocalDate, Attendance>> groupAttendancesByStudent(List<Attendance> attendances) {
        return attendances.stream().collect(Collectors.groupingBy(
                a -> a.getStudent().getId(),
                Collectors.toMap(Attendance::getAttendanceDate, a -> a)));
    }

    private StudentAttendanceRowRes buildStudentRow(Student student,
                                                     Map<LocalDate, Attendance> studentAttendances) {
        Map<LocalDate, AttendanceInfo> infoMap = studentAttendances.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> AttendanceInfo.builder()
                                .id(e.getValue().getId())
                                .status(e.getValue().getStatus())
                                .checkinAt(e.getValue().getCheckinAt())
                                .checkoutAt(e.getValue().getCheckoutAt())
                                .build()
                ));

        return StudentAttendanceRowRes.builder()
                .id(student.getId())
                .name(student.getName())
                .photoUrl(student.getPhotoUrl())
                .className(null)
                .attendances(infoMap)
                .build();
    }

    private void validateAndCollectStudentsForCheckIn(Long tenantId, Long dojangId, List<Long> studentIds,
                                                       List<Student> validStudents, List<BulkCheckFailureRes> failures) {
        Map<Long, Student> studentMap = findStudentsAsMap(studentIds, tenantId);
        Set<Long> alreadyCheckedSet = findAlreadyCheckedStudentIds(tenantId, dojangId, studentIds);

        for (Long studentId : studentIds) {
            validateAndCollectStudent(studentId, dojangId, studentMap, alreadyCheckedSet, validStudents, failures);
        }
    }

    private List<Attendance> createAndSaveBulkAttendances(List<Student> students, CheckMethod method, Long dojangId) {
        LocalDateTime now = LocalDateTime.now();
        List<Attendance> attendances = students.stream()
                .map(student -> Attendance.create(student, method, now, null))
                .toList();

        try {
            attendanceRepository.saveAll(attendances);
        } catch (DataIntegrityViolationException e) {
            log.error("일괄 출석 체크 중복 발생 - dojangId: {}", dojangId, e);
            throw new BusinessException(AttendanceErrorCode.DUPLICATE);
        }
        return attendances;
    }

    private BulkCheckRes buildBulkCheckRes(List<Attendance> attendances, List<BulkCheckFailureRes> failures) {
        List<AttendanceRes> successList = attendances.stream()
                .map(AttendanceRes::from)
                .toList();

        return BulkCheckRes.builder()
                .successCount(successList.size())
                .failureCount(failures.size())
                .successList(successList)
                .failureList(failures)
                .build();
    }

    private List<Attendance> findCheckableAttendances(Long tenantId, Long dojangId, List<Long> attendanceIds) {
        return attendanceRepository.findByTenantIdAndDojangIdAndIdInAndCheckoutAtIsNull(tenantId, dojangId, attendanceIds);
    }

    private List<BulkCheckFailureRes> buildNotFoundFailures(List<Long> requestedIds, List<Attendance> foundAttendances) {
        Set<Long> foundIds = foundAttendances.stream()
                .map(Attendance::getId)
                .collect(Collectors.toSet());

        return requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .map(id -> buildFailure(id, AttendanceErrorCode.NOT_FOUND.getMessage()))
                .toList();
    }

    private void processCheckOut(List<Attendance> attendances) {
        LocalDateTime now = LocalDateTime.now();
        attendances.forEach(attendance -> attendance.checkOut(now));
    }

    private void validateAndChangeStatus(List<Long> attendanceIds, List<Attendance> attendances,
                                          AttendanceStatus status, String note,
                                          List<Attendance> successAttendances, List<BulkCheckFailureRes> failures) {
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(Attendance::getId, a -> a));

        for (Long attendanceId : attendanceIds) {
            Attendance attendance = attendanceMap.get(attendanceId);
            if (attendance == null) {
                failures.add(buildFailure(attendanceId, AttendanceErrorCode.NOT_FOUND.getMessage()));
            } else if (attendance.getStatus() == status) {
                failures.add(buildFailure(attendanceId, AttendanceErrorCode.SAME_STATUS.getMessage()));
            } else {
                attendance.changeStatus(status, note);
                successAttendances.add(attendance);
            }
        }
    }

    private List<Attendance> createAbsenceRecords(List<Student> students, LocalDate date) {
        return students.stream()
                .map(student -> Attendance.createAutoAbsent(student, date))
                .toList();
    }

    private void publishCheckedEvent(Attendance attendance, Long tenantId, boolean isCheckin) {
        eventPublisher.publishEvent(new AttendanceCheckedEvent(
                attendance.getId(),
                attendance.getStudent().getId(),
                tenantId,
                isCheckin
        ));
    }

    private void publishBulkCheckedEvents(List<Attendance> attendances, Long tenantId, boolean isCheckin) {
        for (Attendance attendance : attendances) {
            publishCheckedEvent(attendance, tenantId, isCheckin);
        }
    }
}
