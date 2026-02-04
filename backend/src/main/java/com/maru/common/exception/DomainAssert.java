package com.maru.common.exception;

public final class DomainAssert {

    private DomainAssert() {}

    public static void notNull(Object value, BaseErrorCode errorCode) {
        if (value == null) {
            throw new BusinessException(errorCode);
        }
    }

    public static void hasText(String value, BaseErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(errorCode);
        }
    }

    public static void isTrue(boolean condition, BaseErrorCode errorCode) {
        if (!condition) {
            throw new BusinessException(errorCode);
        }
    }

    public static void isFalse(boolean condition, BaseErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }
}
