package com.maru.common.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

import static java.time.DayOfWeek.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScheduleDaysConverter")
class ScheduleDaysConverterTest {

    private ScheduleDaysConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ScheduleDaysConverter();
    }

    @Nested
    @DisplayName("convertToDatabaseColumn - Set → Integer")
    class ToDatabaseTest {

        @Test
        @DisplayName("월수금 → 21")
        void mon_wed_fri_to_21() {
            // given
            Set<DayOfWeek> days = EnumSet.of(MONDAY, WEDNESDAY, FRIDAY);

            // when
            Integer result = converter.convertToDatabaseColumn(days);

            // then
            assertThat(result).isEqualTo(21);
        }

        @Test
        @DisplayName("화목 → 10")
        void tue_thu_to_10() {
            // given
            Set<DayOfWeek> days = EnumSet.of(TUESDAY, THURSDAY);

            // when
            Integer result = converter.convertToDatabaseColumn(days);

            // then
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("매일 → 127")
        void everyday_to_127() {
            // given
            Set<DayOfWeek> days = EnumSet.allOf(DayOfWeek.class);

            // when
            Integer result = converter.convertToDatabaseColumn(days);

            // then
            assertThat(result).isEqualTo(127);
        }

        @Test
        @DisplayName("빈 Set → null")
        void empty_set_to_null() {
            // given
            Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);

            // when
            Integer result = converter.convertToDatabaseColumn(days);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null → null")
        void null_to_null() {
            // when
            Integer result = converter.convertToDatabaseColumn(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("convertToEntityAttribute - Integer → Set")
    class ToEntityTest {

        @Test
        @DisplayName("21 → 월수금")
        void to_mon_wed_fri() {
            // when
            Set<DayOfWeek> result = converter.convertToEntityAttribute(21);

            // then
            assertThat(result).containsExactlyInAnyOrder(MONDAY, WEDNESDAY, FRIDAY);
        }

        @Test
        @DisplayName("10 → 화목")
        void to_tue_thu() {
            // when
            Set<DayOfWeek> result = converter.convertToEntityAttribute(10);

            // then
            assertThat(result).containsExactlyInAnyOrder(TUESDAY, THURSDAY);
        }

        @Test
        @DisplayName("127 → 모든 요일")
        void to_everyday() {
            // when
            Set<DayOfWeek> result = converter.convertToEntityAttribute(127);

            // then
            assertThat(result).containsExactlyInAnyOrder(
                    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
            );
        }

        @Test
        @DisplayName("0 → 빈 Set")
        void zero_to_empty_set() {
            // when
            Set<DayOfWeek> result = converter.convertToEntityAttribute(0);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null → 빈 Set")
        void null_to_empty_set() {
            // when
            Set<DayOfWeek> result = converter.convertToEntityAttribute(null);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTest {

        @Test
        @DisplayName("Set → Integer → Set 변환 후 동일")
        void round_trip_preserves_data() {
            // given
            Set<DayOfWeek> original = EnumSet.of(MONDAY, WEDNESDAY, FRIDAY);

            // when
            Integer dbValue = converter.convertToDatabaseColumn(original);
            Set<DayOfWeek> restored = converter.convertToEntityAttribute(dbValue);

            // then
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("모든 단일 요일 변환 검증")
        void all_single_days_round_trip() {
            for (DayOfWeek day : DayOfWeek.values()) {
                // given
                Set<DayOfWeek> original = EnumSet.of(day);

                // when
                Integer dbValue = converter.convertToDatabaseColumn(original);
                Set<DayOfWeek> restored = converter.convertToEntityAttribute(dbValue);

                // then
                assertThat(restored)
                        .as("요일 %s 변환 검증", day)
                        .containsExactly(day);
            }
        }
    }
}
