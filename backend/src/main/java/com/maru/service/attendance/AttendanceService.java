package com.maru.service.attendance;

import com.maru.common.aop.SkipDojangValidation;
import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.domain.permission.PermissionType;
import com.maru.security.RequirePermission;
import com.maru.controller.attendance.dto.*;
import com.maru.domain.attendance.Attendance;
import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;
import com.maru.domain.attendance.exception.AttendanceErrorCode;
import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageType;
import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.attendance.AttendanceRepository;
import com.maru.repository.attendance.view.NotificationTargetView;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.guardian.view.StudentGuardianIdView;
import com.maru.repository.message.MessageDispatchRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.student.view.StudentMinimalView;
import com.maru.security.TenantContextHolder;
import com.maru.service.enrollment.EnrollmentQueryService;
import com.maru.service.notification.MessageContentRenderer;
import com.maru.service.notification.MessageContentRenderer.RenderedContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@ValidateDojangAccess
public class AttendanceService {

    private static final int MAX_DATE_RANGE_DAYS = 31;
    private static final int MAX_RETROACTIVE_DAYS = 30;
    private static final LocalTime DEFAULT_CHECKIN_TIME = LocalTime.of(9, 0);

    private static final String REF_TYPE_ATTENDANCE = "ATTENDANCE";

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentQueryService enrollmentQueryService;
    private final AttendanceQueryService attendanceQueryService;
    private final MessageDispatchRepository messageDispatchRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final MessageContentRenderer contentRenderer;

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
    @RequirePermission(PermissionType.ATTENDANCE_UPDATE)
    @Transactional
    public AttendanceRes checkIn(String dojangId, String studentId, CheckMethod method, AttendanceStatus status,
                                  LocalDate date, LocalDateTime checkinAt, String note) {
        String tenantId = TenantContextHolder.getTenantId();
        validateStudentInDojang(studentId, dojangId, tenantId);
        LocalDate targetDate = resolveTargetDate(date);

        AttendanceStatus targetStatus = status != null ? status : AttendanceStatus.PRESENT;
        LocalDateTime targetCheckinAt = targetStatus == AttendanceStatus.PRESENT
                ? resolveCheckinAt(targetDate, checkinAt)
                : targetDate.atTime(DEFAULT_CHECKIN_TIME);

        Attendance attendance = saveAttendanceWithDuplicateCheck(
                tenantId, dojangId, studentId, method, targetStatus, targetCheckinAt, note);

        saveOutboxMessages(tenantId, dojangId, studentId, attendance.getId(), MessageType.ATTENDANCE_CHECKIN);

        log.info("출석 기록 생성 - attendanceId: {}, studentId: {}, status: {}, dojangId: {}",
                attendance.getId(), studentId, targetStatus, dojangId);
        return attendanceQueryService.getAttendance(tenantId, dojangId, attendance.getId());
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
    @RequirePermission(PermissionType.ATTENDANCE_UPDATE)
    @Transactional
    public BulkCheckRes bulkCheckIn(String dojangId, List<String> studentIds, CheckMethod method) {
        String tenantId = TenantContextHolder.getTenantId();

        List<Student> validStudents = new ArrayList<>();
        List<BulkCheckFailureRes> failures = new ArrayList<>();
        validateAndCollectStudentsForCheckIn(tenantId, dojangId, studentIds, validStudents, failures);

        List<Attendance> attendances = createAndSaveBulkAttendances(tenantId, dojangId, validStudents, method);

        saveBatchOutboxMessages(tenantId, dojangId, attendances, MessageType.ATTENDANCE_CHECKIN);

        log.info("일괄 출석 체크 - dojangId: {}, success: {}, failure: {}", dojangId, attendances.size(), failures.size());
        return buildBulkCheckRes(tenantId, dojangId, attendances, failures);
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
    @RequirePermission(PermissionType.ATTENDANCE_UPDATE)
    @Transactional
    public AttendanceRes checkOut(String dojangId, String attendanceId) {
        String tenantId = TenantContextHolder.getTenantId();
        Attendance attendance = findAttendanceInDojang(tenantId, attendanceId, dojangId);

        attendance.checkOut(LocalDateTime.now());

        saveOutboxMessages(tenantId, dojangId, attendance.getStudentId(), attendanceId, MessageType.ATTENDANCE_CHECKOUT);

        log.info("퇴관 처리 - attendanceId: {}, dojangId: {}", attendanceId, dojangId);
        return attendanceQueryService.getAttendance(tenantId, dojangId, attendanceId);
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
    @RequirePermission(PermissionType.ATTENDANCE_UPDATE)
    @Transactional
    public AttendanceRes cancelCheckout(String dojangId, String attendanceId) {
        String tenantId = TenantContextHolder.getTenantId();
        Attendance attendance = findAttendanceInDojang(tenantId, attendanceId, dojangId);

        attendance.cancelCheckout();

        log.info("퇴관 취소 - attendanceId: {}, dojangId: {}", attendanceId, dojangId);
        return attendanceQueryService.getAttendance(tenantId, dojangId, attendanceId);
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
    @RequirePermission(PermissionType.ATTENDANCE_UPDATE)
    @Transactional
    public BulkCheckRes bulkCheckOut(String dojangId, List<String> attendanceIds) {
        String tenantId = TenantContextHolder.getTenantId();

        List<Attendance> attendances = findCheckableAttendances(tenantId, dojangId, attendanceIds);
        List<BulkCheckFailureRes> failures = buildNotFoundFailures(attendanceIds, attendances);

        processCheckOut(attendances);

        saveBatchOutboxMessages(tenantId, dojangId, attendances, MessageType.ATTENDANCE_CHECKOUT);

        log.info("일괄 퇴관 처리 - dojangId: {}, success: {}, failure: {}", dojangId, attendances.size(), failures.size());
        return buildBulkCheckRes(tenantId, dojangId, attendances, failures);
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
    @RequirePermission(PermissionType.ATTENDANCE_UPDATE)
    @Transactional
    public AttendanceRes changeStatus(String dojangId, String attendanceId, AttendanceStatus status, String note) {
        String tenantId = TenantContextHolder.getTenantId();
        Attendance attendance = findAttendanceInDojang(tenantId, attendanceId, dojangId);

        attendance.changeStatus(status, note);

        log.info("출석 상태 변경 - attendanceId: {}, status: {}, dojangId: {}", attendanceId, status, dojangId);
        return attendanceQueryService.getAttendance(tenantId, dojangId, attendanceId);
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
    @RequirePermission(PermissionType.ATTENDANCE_UPDATE)
    @Transactional
    public AttendanceRes changeTime(String dojangId, String attendanceId, LocalDateTime checkinAt, LocalDateTime checkoutAt) {
        String tenantId = TenantContextHolder.getTenantId();
        Attendance attendance = findAttendanceInDojang(tenantId, attendanceId, dojangId);

        attendance.changeTime(checkinAt, checkoutAt);

        log.info("출석 시간 변경 - attendanceId: {}, checkinAt: {}, checkoutAt: {}, dojangId: {}",
                attendanceId, checkinAt, checkoutAt, dojangId);
        return attendanceQueryService.getAttendance(tenantId, dojangId, attendanceId);
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
    @RequirePermission(PermissionType.ATTENDANCE_UPDATE)
    @Transactional
    public BulkCheckRes bulkChangeStatus(String dojangId, List<String> attendanceIds, AttendanceStatus status, String note) {
        String tenantId = TenantContextHolder.getTenantId();

        List<Attendance> attendances = attendanceRepository.findByTenantIdAndDojangIdAndIdIn(tenantId, dojangId, attendanceIds);

        List<Attendance> successAttendances = new ArrayList<>();
        List<BulkCheckFailureRes> failures = new ArrayList<>();
        validateAndChangeStatus(attendanceIds, attendances, status, note, successAttendances, failures);

        log.info("일괄 출석 상태 변경 - dojangId: {}, status: {}, success: {}, failure: {}",
                dojangId, status, successAttendances.size(), failures.size());
        return buildBulkCheckRes(tenantId, dojangId, successAttendances, failures);
    }

    /**
     * 기간별 출석 현황 조회 (7일/1개월 뷰)
     *
     * @param dojangId 도장 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜 (최대 31일 범위)
     * @param sectionId 수련부 ID (선택, 해당 수련부 소속 원생만 조회)
     * @param divisionId 수련반 ID (선택, 해당 수련반 소속 원생만 조회)
     * @return 기간별 출석 현황
     * @throws BusinessException DOJANG_NOT_FOUND - 도장을 찾을 수 없음
     * @throws BusinessException DOJANG_UNAUTHORIZED_ACCESS - 도장 접근 권한 없음
     * @throws BusinessException ATTENDANCE_DATE_RANGE_INVALID - 시작일이 종료일보다 이후
     * @throws BusinessException ATTENDANCE_DATE_RANGE_TOO_LARGE - 조회 기간 31일 초과
     */
    @RequirePermission(PermissionType.ATTENDANCE_VIEW)
    @Transactional(readOnly = true)
    public RangeAttendanceRes getAttendanceRange(String dojangId, LocalDate startDate, LocalDate endDate,
                                                  String sectionId, String divisionId) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDateRange(startDate, endDate);

        List<String> filteredStudentIds = enrollmentQueryService.getStudentIdsByFilter(dojangId, sectionId, divisionId);

        return attendanceQueryService.getAttendanceRange(tenantId, dojangId, startDate, endDate, filteredStudentIds);
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
    @RequirePermission(PermissionType.ATTENDANCE_VIEW)
    @Transactional(readOnly = true)
    public List<AttendanceRes> getHistory(String dojangId, String studentId, LocalDate startDate, LocalDate endDate) {
        String tenantId = TenantContextHolder.getTenantId();
        validateDateRange(startDate, endDate);
        validateStudentInDojang(studentId, dojangId, tenantId);

        return attendanceQueryService.getHistory(tenantId, dojangId, studentId, startDate, endDate);
    }

    /**
     * 자동 결석 처리
     *
     * @param date 처리할 날짜
     * @return 처리된 결석 건수
     */
    @Transactional
    @SkipDojangValidation
    public int processAutoAbsence(LocalDate date) {
        List<StudentMinimalView> students = studentRepository.findAllActiveStudentsWithoutAttendanceMinimal(date);

        if (students.isEmpty()) {
            log.info("자동 결석 처리 대상 없음: date={}", date);
            return 0;
        }

        List<Attendance> absences = createAbsenceRecords(students, date);
        attendanceRepository.saveAll(absences);

        log.info("자동 결석 처리 완료: date={}, count={}", date, absences.size());
        return absences.size();
    }

    private void validateStudentInDojang(String studentId, String dojangId, String tenantId) {
        if (!studentRepository.existsActiveByIdAndDojangId(studentId, tenantId, dojangId, StudentStatus.WITHDRAWN)) {
            throw new BusinessException(StudentErrorCode.NOT_FOUND);
        }
    }

    private Attendance findAttendanceInDojang(String tenantId, String attendanceId, String dojangId) {
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

    private Attendance saveAttendanceWithDuplicateCheck(String tenantId, String dojangId, String studentId,
                                                         CheckMethod method, AttendanceStatus status,
                                                         LocalDateTime checkinAt, String note) {
        Attendance attendance = Attendance.create(tenantId, dojangId, studentId, method, status, checkinAt, note);
        try {
            return attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(AttendanceErrorCode.DUPLICATE);
        }
    }

    private Map<String, Student> findStudentsAsMap(List<String> studentIds, String tenantId) {
        return studentRepository.findAllActiveByIds(studentIds, tenantId, StudentStatus.WITHDRAWN)
                .stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
    }

    private Set<String> findAlreadyCheckedStudentIds(String tenantId, String dojangId, List<String> studentIds) {
        return new HashSet<>(attendanceRepository.findStudentIdsWithAttendanceToday(
                tenantId, dojangId, studentIds, LocalDate.now()));
    }

    private void validateAndCollectStudent(String studentId, String dojangId, Map<String, Student> studentMap,
                                           Set<String> alreadyCheckedSet, List<Student> validStudents,
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
        if (!student.getDojangId().equals(dojangId)) {
            failureList.add(buildFailure(studentId, DojangErrorCode.UNAUTHORIZED_ACCESS.getMessage()));
            return;
        }

        validStudents.add(student);
    }

    private BulkCheckFailureRes buildFailure(String id, String message) {
        return BulkCheckFailureRes.builder().studentId(id).errorMessage(message).build();
    }

    private void validateAndCollectStudentsForCheckIn(String tenantId, String dojangId, List<String> studentIds,
                                                       List<Student> validStudents, List<BulkCheckFailureRes> failures) {
        Map<String, Student> studentMap = findStudentsAsMap(studentIds, tenantId);
        Set<String> alreadyCheckedSet = findAlreadyCheckedStudentIds(tenantId, dojangId, studentIds);

        for (String studentId : studentIds) {
            validateAndCollectStudent(studentId, dojangId, studentMap, alreadyCheckedSet, validStudents, failures);
        }
    }

    private List<Attendance> createAndSaveBulkAttendances(String tenantId, String dojangId,
                                                           List<Student> students, CheckMethod method) {
        LocalDateTime now = LocalDateTime.now();
        List<Attendance> attendances = students.stream()
                .map(student -> Attendance.create(tenantId, dojangId, student.getId(), method, now, null))
                .toList();

        try {
            attendanceRepository.saveAll(attendances);
        } catch (DataIntegrityViolationException e) {
            log.error("일괄 출석 체크 중복 발생 - dojangId: {}", dojangId, e);
            throw new BusinessException(AttendanceErrorCode.DUPLICATE);
        }
        return attendances;
    }

    private BulkCheckRes buildBulkCheckRes(String tenantId, String dojangId, List<Attendance> attendances,
                                            List<BulkCheckFailureRes> failures) {
        List<String> attendanceIds = attendances.stream()
                .map(Attendance::getId)
                .toList();
        List<AttendanceRes> successList = attendanceQueryService.getAttendances(tenantId, dojangId, attendanceIds);

        return BulkCheckRes.builder()
                .successCount(successList.size())
                .failureCount(failures.size())
                .successList(successList)
                .failureList(failures)
                .build();
    }

    private List<Attendance> findCheckableAttendances(String tenantId, String dojangId, List<String> attendanceIds) {
        return attendanceRepository.findByTenantIdAndDojangIdAndIdInAndCheckoutAtIsNull(tenantId, dojangId, attendanceIds);
    }

    private List<BulkCheckFailureRes> buildNotFoundFailures(List<String> requestedIds, List<Attendance> foundAttendances) {
        Set<String> foundIds = foundAttendances.stream()
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

    private void validateAndChangeStatus(List<String> attendanceIds, List<Attendance> attendances,
                                          AttendanceStatus status, String note,
                                          List<Attendance> successAttendances, List<BulkCheckFailureRes> failures) {
        Map<String, Attendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(Attendance::getId, a -> a));

        for (String attendanceId : attendanceIds) {
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

    private List<Attendance> createAbsenceRecords(List<StudentMinimalView> students, LocalDate date) {
        return students.stream()
                .map(student -> Attendance.createAutoAbsent(
                        student.getTenantId(), student.getDojangId(), student.getId(), date))
                .toList();
    }

    private void saveOutboxMessages(String tenantId, String dojangId, String studentId,
                                     String attendanceId, MessageType messageType) {
        List<String> guardianIds = guardianshipRepository.findPrimaryGuardianIdsByStudentId(studentId);

        if (guardianIds.isEmpty()) {
            log.debug("알림 대상 보호자 없음: studentId={}", studentId);
            return;
        }

        NotificationTargetView target = attendanceRepository.findNotificationTarget(attendanceId)
                .orElse(null);
        if (target == null) {
            log.warn("출결 정보 조회 실패로 알림 생성 스킵: attendanceId={}", attendanceId);
            return;
        }

        RenderedContent content = contentRenderer.renderAttendance(messageType, target);

        List<MessageDispatch> outboxMessages = guardianIds.stream()
                .map(guardianId -> MessageDispatch.createPending(
                        tenantId, dojangId, guardianId,
                        REF_TYPE_ATTENDANCE, attendanceId,
                        messageType, MessageChannel.SMS,
                        content.title(), content.body(), content.dataPayload()))
                .toList();

        messageDispatchRepository.saveAll(outboxMessages);
        log.debug("outbox 메시지 생성: attendanceId={}, count={}", attendanceId, outboxMessages.size());
    }

    private void saveBatchOutboxMessages(String tenantId, String dojangId,
                                          List<Attendance> attendances, MessageType messageType) {
        if (attendances.isEmpty()) {
            return;
        }

        List<String> studentIds = attendances.stream()
                .map(Attendance::getStudentId)
                .distinct()
                .toList();
        List<String> attendanceIds = attendances.stream()
                .map(Attendance::getId)
                .toList();

        Map<String, List<String>> guardianMap = buildGuardianMap(studentIds);
        Map<String, NotificationTargetView> targetMap = buildTargetMap(attendanceIds);

        List<MessageDispatch> allMessages = new ArrayList<>();
        for (Attendance attendance : attendances) {
            NotificationTargetView target = targetMap.get(attendance.getId());
            if (target == null) {
                log.warn("출결 정보 조회 실패로 알림 생성 스킵: attendanceId={}", attendance.getId());
                continue;
            }

            List<String> guardianIds = guardianMap.getOrDefault(attendance.getStudentId(), List.of());
            if (guardianIds.isEmpty()) {
                log.debug("알림 대상 보호자 없음: studentId={}", attendance.getStudentId());
                continue;
            }

            RenderedContent content = contentRenderer.renderAttendance(messageType, target);

            for (String guardianId : guardianIds) {
                allMessages.add(MessageDispatch.createPending(
                        tenantId, dojangId, guardianId,
                        REF_TYPE_ATTENDANCE, attendance.getId(),
                        messageType, MessageChannel.SMS,
                        content.title(), content.body(), content.dataPayload()));
            }
        }

        if (!allMessages.isEmpty()) {
            messageDispatchRepository.saveAll(allMessages);
            log.debug("배치 outbox 메시지 생성: count={}", allMessages.size());
        }
    }

    private Map<String, List<String>> buildGuardianMap(List<String> studentIds) {
        return guardianshipRepository.findPrimaryGuardianIdsByStudentIds(studentIds)
                .stream()
                .collect(Collectors.groupingBy(
                        StudentGuardianIdView::getStudentId,
                        Collectors.mapping(StudentGuardianIdView::getGuardianId, Collectors.toList())));
    }

    private Map<String, NotificationTargetView> buildTargetMap(List<String> attendanceIds) {
        return attendanceRepository.findNotificationTargets(attendanceIds)
                .stream()
                .collect(Collectors.toMap(NotificationTargetView::getId, t -> t));
    }
}
