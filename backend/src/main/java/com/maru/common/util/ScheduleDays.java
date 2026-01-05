package com.maru.common.util;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * 요일 비트마스크 변환 유틸리티
 * MONDAY=1,       TUESDAY=2,       WEDNESDAY=4,       THURSDAY=8,       FRIDAY=16,      SATURDAY=32,      SUNDAY=64
 * MONDAY=0000001, TUESDAY=0000010, WEDNESDAY=0000100, THURSDAY=0001000, FRIDAY=0010000, SATURDAY=0100000, SUNDAY=1000000
 */
public final class ScheduleDays {

    private ScheduleDays() {}

    /**
     * 단일 요일을 비트 값으로 변환
     * @param day 요일
     * @return 비트 값 (MONDAY=1, TUESDAY=2, ...)
     */
    public static int toBit(DayOfWeek day) {
        return 1 << (day.getValue() - 1);
    }

    /**
     * 여러 요일을 비트마스크로 변환
     * @param days 요일 Set
     * @return 비트마스크 (월수금 = 21)
     */
    public static int toBits(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            return 0;
        }
        return days.stream()
                .mapToInt(ScheduleDays::toBit)
                .sum();
    }

    /**
     * 여러 요일을 비트마스크로 변환 (가변인자)
     * @param days 요일들
     * @return 비트마스크 (월수금 = 21)
     */
    public static int toBits(DayOfWeek... days) {
        if (days == null || days.length == 0) {
            return 0;
        }
        return toBits(EnumSet.copyOf(Arrays.asList(days)));
    }
}
