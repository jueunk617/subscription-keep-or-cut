package com.back.global.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth attribute) {
        // 자바(YearMonth) -> DB(String)
        return (attribute != null) ? attribute.toString() : null;
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        // DB(String) -> 자바(YearMonth)
        return (dbData != null) ? YearMonth.parse(dbData) : null;
    }
}