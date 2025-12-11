package com.maru.service.attendance;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.ErrorCode;
import com.maru.controller.attendance.dto.*;
import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    /**
     * 단일 원생 출석 체크 (과거 날짜 소급 입력 지원)
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param method 체크 방법
     * @param date 출석 날짜 (null이면 오늘, 과거 30일 이내)
     * @param checkinAt 체크인 시각 (null이면 현재 시각 또는 date의 기본 시각)
     * @param note 비고 (선택)
     * @return 출석 기록
     * @throws BusinessException 원생을 찾을 수 없거나 중복 체크인 경우
     */
    @Transactional
    public AttendanceRes checkIn(
            Long dojangId,
            Long studentId,
            CheckMethod method,
            LocalDate date,
            LocalDateTime checkinAt,
            String note) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 일괄 출석 체크
     *
     * @param dojangId 도장 ID
     * @param studentIds 원생 ID 목록
     * @param method 체크 방법
     * @return 성공/실패 결과
     */
    @Transactional
    public BulkCheckRes bulkCheckIn(Long dojangId, List<Long> studentIds, CheckMethod method) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 퇴관 처리
     *
     * @param dojangId 도장 ID
     * @param attendanceId 출석 기록 ID
     * @return 업데이트된 출석 기록
     * @throws BusinessException 출석 기록을 찾을 수 없거나 이미 퇴관 처리된 경우
     */
    @Transactional
    public AttendanceRes checkOut(Long dojangId, Long attendanceId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 출석 상태 변경
     *
     * @param dojangId 도장 ID
     * @param attendanceId 출석 기록 ID
     * @param status 새로운 상태
     * @param note 비고 (선택)
     * @return 업데이트된 출석 기록
     * @throws BusinessException 출석 기록을 찾을 수 없거나 유효하지 않은 상태 전이인 경우
     */
    @Transactional
    public AttendanceRes changeStatus(
            Long dojangId,
            Long attendanceId,
            AttendanceStatus status,
            String note) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 오늘 출석 현황 조회
     *
     * @param dojangId 도장 ID
     * @return 현재 출석 현황
     */
    @Transactional(readOnly = true)
    public CurrentAttendanceRes getTodayAttendance(Long dojangId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 기간별 출석 현황 조회 (7일/1개월 뷰)
     *
     * @param dojangId 도장 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜 (최대 31일 범위)
     * @return 기간별 출석 현황
     */
    @Transactional(readOnly = true)
    public RangeAttendanceRes getAttendanceRange(Long dojangId, LocalDate startDate, LocalDate endDate) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 출석 이력 조회
     *
     * @param dojangId 도장 ID
     * @param studentId 원생 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 출석 이력 목록
     */
    @Transactional(readOnly = true)
    public List<AttendanceRes> getHistory(
            Long dojangId,
            Long studentId,
            LocalDate startDate,
            LocalDate endDate) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 월간 출석 통계 조회
     *
     * @param dojangId 도장 ID
     * @param yearMonth 연월
     * @return 월간 통계
     */
    @Transactional(readOnly = true)
    public AttendanceStatsRes getMonthlyStats(Long dojangId, YearMonth yearMonth) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
