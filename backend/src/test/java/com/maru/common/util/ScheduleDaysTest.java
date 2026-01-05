package com.maru.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

import static java.time.DayOfWeek.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScheduleDays 유틸리티")
class ScheduleDaysTest {

    @Nested
    @DisplayName("toBit - 단일 요일 변환")
    class ToBitTest {

        @Test
        @DisplayName("월요일 = 1")
        void monday_returns_1() {
            assertThat(ScheduleDays.toBit(MONDAY)).isEqualTo(1);
        }

        @Test
        @DisplayName("화요일 = 2")
        void tuesday_returns_2() {
            assertThat(ScheduleDays.toBit(TUESDAY)).isEqualTo(2);
        }

        @Test
        @DisplayName("수요일 = 4")
        void wednesday_returns_4() {
            assertThat(ScheduleDays.toBit(WEDNESDAY)).isEqualTo(4);
        }

        @Test
        @DisplayName("목요일 = 8")
        void thursday_returns_8() {
            assertThat(ScheduleDays.toBit(THURSDAY)).isEqualTo(8);
        }

        @Test
        @DisplayName("금요일 = 16")
        void friday_returns_16() {
            assertThat(ScheduleDays.toBit(FRIDAY)).isEqualTo(16);
        }

        @Test
        @DisplayName("토요일 = 32")
        void saturday_returns_32() {
            assertThat(ScheduleDays.toBit(SATURDAY)).isEqualTo(32);
        }

        @Test
        @DisplayName("일요일 = 64")
        void sunday_returns_64() {
            assertThat(ScheduleDays.toBit(SUNDAY)).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("toBits(Set) - 다중 요일 변환")
    class ToBitsSetTest {

        @Test
        @DisplayName("월수금 = 21 (1 + 4 + 16)")
        void mon_wed_fri_returns_21() {
            Set<DayOfWeek> days = EnumSet.of(MONDAY, WEDNESDAY, FRIDAY);
            assertThat(ScheduleDays.toBits(days)).isEqualTo(21);
        }

        @Test
        @DisplayName("화목 = 10 (2 + 8)")
        void tue_thu_returns_10() {
            Set<DayOfWeek> days = EnumSet.of(TUESDAY, THURSDAY);
            assertThat(ScheduleDays.toBits(days)).isEqualTo(10);
        }

        @Test
        @DisplayName("주말 = 96 (32 + 64)")
        void weekend_returns_96() {
            Set<DayOfWeek> days = EnumSet.of(SATURDAY, SUNDAY);
            assertThat(ScheduleDays.toBits(days)).isEqualTo(96);
        }

        @Test
        @DisplayName("매일 = 127 (1+2+4+8+16+32+64)")
        void everyday_returns_127() {
            Set<DayOfWeek> days = EnumSet.allOf(DayOfWeek.class);
            assertThat(ScheduleDays.toBits(days)).isEqualTo(127);
        }

        @Test
        @DisplayName("빈 Set = 0")
        void empty_set_returns_0() {
            Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
            assertThat(ScheduleDays.toBits(days)).isEqualTo(0);
        }

        @Test
        @DisplayName("null = 0")
        void null_returns_0() {
            assertThat(ScheduleDays.toBits((Set<DayOfWeek>) null)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("toBits(varargs) - 가변인자 변환")
    class ToBitsVarargsTest {

        @Test
        @DisplayName("월수금 = 21")
        void mon_wed_fri_returns_21() {
            assertThat(ScheduleDays.toBits(MONDAY, WEDNESDAY, FRIDAY)).isEqualTo(21);
        }

        @Test
        @DisplayName("화목 = 10")
        void tue_thu_returns_10() {
            assertThat(ScheduleDays.toBits(TUESDAY, THURSDAY)).isEqualTo(10);
        }

        @Test
        @DisplayName("단일 요일도 가능")
        void single_day_works() {
            assertThat(ScheduleDays.toBits(MONDAY)).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 인자 = 0")
        void no_args_returns_0() {
            assertThat(ScheduleDays.toBits()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("비트 연산 검증")
    class BitOperationTest {

        @Test
        @DisplayName("월수금(21)에 월요일 포함 여부 확인")
        void check_monday_included_in_21() {
            int monWedFri = ScheduleDays.toBits(MONDAY, WEDNESDAY, FRIDAY);
            int monday = ScheduleDays.toBit(MONDAY);

            assertThat((monWedFri & monday) > 0).isTrue();
        }

        @Test
        @DisplayName("월수금(21)에 화요일 미포함 확인")
        void check_tuesday_not_included_in_21() {
            int monWedFri = ScheduleDays.toBits(MONDAY, WEDNESDAY, FRIDAY);
            int tuesday = ScheduleDays.toBit(TUESDAY);

            assertThat((monWedFri & tuesday) > 0).isFalse();
        }

        @Test
        @DisplayName("월수금(21)이 월수(5) 모두 포함하는지 확인")
        void check_mon_wed_all_included() {
            int monWedFri = ScheduleDays.toBits(MONDAY, WEDNESDAY, FRIDAY);
            int monWed = ScheduleDays.toBits(MONDAY, WEDNESDAY);

            assertThat((monWedFri & monWed) == monWed).isTrue();
        }
    }
}
