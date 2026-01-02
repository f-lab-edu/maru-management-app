package com.maru.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthConverter implements AttributeConverter<YearMonth, Integer> {

    @Override
    public Integer convertToDatabaseColumn(YearMonth yearMonth) {
        if (yearMonth == null) {
            return null;
        }
        return yearMonth.getYear() * 100 + yearMonth.getMonthValue();
    }

    @Override
    public YearMonth convertToEntityAttribute(Integer dbValue) {
        if (dbValue == null) {
            return null;
        }
        int year = dbValue / 100;
        int month = dbValue % 100;
        return YearMonth.of(year, month);
    }
}
