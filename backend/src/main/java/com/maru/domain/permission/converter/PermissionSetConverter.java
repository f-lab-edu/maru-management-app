package com.maru.domain.permission.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maru.domain.permission.PermissionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Converter
public class PermissionSetConverter implements AttributeConverter<Set<PermissionType>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Set<PermissionType>을 JSON 문자열로 변환
     *
     * @param attribute 권한 Set
     * @return JSON 문자열 (예: ["STUDENT_VIEW", "ATTENDANCE_CHECK"])
     */
    @Override
    public String convertToDatabaseColumn(Set<PermissionType> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }

        try {
            List<String> names = attribute.stream()
                    .map(Enum::name)
                    .sorted()
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(names);
        } catch (JsonProcessingException e) {
            log.error("권한 Set을 JSON으로 변환 실패: {}", attribute, e);
            return "[]";
        }
    }

    /**
     * JSON 문자열을 Set<PermissionType>으로 변환
     *
     * @param dbData JSON 문자열
     * @return 권한 Set (파싱 실패 시 빈 Set 반환)
     */
    @Override
    public Set<PermissionType> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || "[]".equals(dbData)) {
            return new HashSet<>();
        }

        try {
            List<String> names = objectMapper.readValue(dbData, new TypeReference<>() {});
            return names.stream()
                    .map(name -> {
                        try {
                            return PermissionType.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            log.error("알 수 없는 권한 타입: {}", name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (JsonProcessingException e) {
            log.error("JSON을 권한 Set으로 변환 실패: {}", dbData, e);
            return new HashSet<>();
        }
    }
}
