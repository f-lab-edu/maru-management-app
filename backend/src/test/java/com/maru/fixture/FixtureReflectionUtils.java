package com.maru.fixture;

import java.lang.reflect.Field;

public final class FixtureReflectionUtils {

    private FixtureReflectionUtils() {
    }

    public static void setId(Object entity, String id) {
        setField(entity, "id", id);
    }

    public static void setField(Object entity, String fieldName, Object value) {
        try {
            Field field = findField(entity.getClass(), fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Fixture 필드 세팅 실패: " + fieldName, e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
