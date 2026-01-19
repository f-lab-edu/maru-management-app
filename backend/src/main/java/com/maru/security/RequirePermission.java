package com.maru.security;

import com.maru.domain.permission.PermissionType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 전 권한 검증을 수행하는 어노테이션
 *
 * @see com.maru.common.aop.PermissionCheckAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 필요한 권한 목록
     */
    PermissionType[] value();

    /**
     * 권한 검증 연산자 (기본값: OR)
     */
    LogicalOperator operator() default LogicalOperator.OR;

    enum LogicalOperator {
        /** 하나라도 있으면 통과 */
        OR,
        /** 모두 있어야 통과 */
        AND
    }
}
