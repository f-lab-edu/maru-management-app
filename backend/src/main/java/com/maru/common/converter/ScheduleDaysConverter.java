package com.maru.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Set<DayOfWeek>와 비트마스크 Integer 간 변환
 * MONDAY=1,       TUESDAY=2,       WEDNESDAY=4,       THURSDAY=8,       FRIDAY=16,      SATURDAY=32,      SUNDAY=64
 * MONDAY=0000001, TUESDAY=0000010, WEDNESDAY=0000100, THURSDAY=0001000, FRIDAY=0010000, SATURDAY=0100000, SUNDAY=1000000
 */
@Converter
public class ScheduleDaysConverter implements AttributeConverter<Set<DayOfWeek>, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            return null;
        }
        return days.stream()
                .mapToInt(day -> 1 << (day.getValue() - 1))
                .sum();
    }

    @Override
    public Set<DayOfWeek> convertToEntityAttribute(Integer bits) {
        if (bits == null || bits == 0) {
            return EnumSet.noneOf(DayOfWeek.class);
        }
        return Arrays.stream(DayOfWeek.values())
                .filter(day -> (bits & (1 << (day.getValue() - 1))) > 0)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }
}
