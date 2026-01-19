package com.maru.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 날짜 범위를 표현하는 Record
 * 배열 인덱스 접근 대신 명시적인 start(), end() 메서드 제공
 */
public record DateRange(LocalDateTime start, LocalDateTime end) {

    /**
     * LocalDate 범위를 LocalDateTime 범위로 변환
     * endDate는 다음 날 00:00:00으로 설정하여 해당 일자 전체 포함
     *
     * @param startDate 시작일
     * @param endDate 종료일
     * @return DateRange 인스턴스
     */
    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        return new DateRange(
            startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay()
        );
    }

    /**
     * 기본값 적용하여 DateRange 생성
     * startDate가 null이면 이번 달 1일, endDate가 null이면 오늘
     *
     * @param startDate 시작일 (null 가능)
     * @param endDate 종료일 (null 가능)
     * @return DateRange 인스턴스
     */
    public static DateRange ofWithDefaults(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStart = (startDate != null)
            ? startDate
            : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEnd = (endDate != null)
            ? endDate
            : LocalDate.now();
        return of(effectiveStart, effectiveEnd);
    }
}
