package com.maru.security;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContextHolder {

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";
    public static final String SYSTEM_USER_ID = "SYSTEM";
    public static final String SYSTEM_TENANT_ID = "SYSTEM";

    private static final ThreadLocal<ContextInfo> CONTEXT = new ThreadLocal<>();
    public record ContextInfo(String tenantId, String userId, String dojangId, String role) {}

    /**
     * 현재 스레드의 컨텍스트 설정 (tenantId, userId, dojangId, role)
     *
     * @param tenantId 테넌트 ID
     * @param userId 사용자 ID
     * @param dojangId 도장 ID
     * @param role 사용자 역할
     * @return AutoCloseable (try-with-resources 사용)
     */
    public static AutoCloseable withContext(String tenantId, String userId, String dojangId, String role) {
        if (CONTEXT.get() != null) {
            throw new IllegalStateException("테넌트 컨텍스트가 이미 설정되어 있음");
        }

        CONTEXT.set(new ContextInfo(tenantId, userId, dojangId, role));
        log.debug("테넌트 컨텍스트 설정: tenantId={}, userId={}, dojangId={}, role={}", tenantId, userId, dojangId, role);
        return TenantContextHolder::clear;
    }

    /**
     * 컨텍스트 설정 (ContextInfo 객체, TaskDecorator용)
     *
     * @param info 컨텍스트 정보
     * @return AutoCloseable (try-with-resources 사용)
     */
    public static AutoCloseable withContext(ContextInfo info) {
        if (info == null) {
            return () -> {};
        }
        return withContext(info.tenantId(), info.userId(), info.dojangId(), info.role());
    }

    /**
     * 현재 스레드의 컨텍스트 정보 전체 조회
     *
     * @return ContextInfo (설정되지 않았으면 null)
     */
    public static ContextInfo getContextInfo() {
        return CONTEXT.get();
    }

    /**
     * 현재 스레드의 테넌트 ID 조회
     *
     * @return 테넌트 ID (설정되지 않았으면 null)
     */
    public static String getTenantId() {
        ContextInfo info = CONTEXT.get();
        String tenantId = info != null ? info.tenantId() : null;
        log.trace("테넌트 컨텍스트 조회: tenantId={}", tenantId);
        return tenantId;
    }

    /**
     * 현재 스레드의 사용자 ID 조회
     *
     * @return 사용자 ID (설정되지 않았으면 null)
     */
    public static String getUserId() {
        ContextInfo info = CONTEXT.get();
        return info != null ? info.userId() : null;
    }

    /**
     * 현재 스레드의 도장 ID 조회
     *
     * @return 도장 ID (설정되지 않았으면 null)
     */
    public static String getDojangId() {
        ContextInfo info = CONTEXT.get();
        return info != null ? info.dojangId() : null;
    }

    /**
     * 현재 스레드의 사용자 역할 조회
     *
     * @return 역할 (설정되지 않았으면 null)
     */
    public static String getRole() {
        ContextInfo info = CONTEXT.get();
        return info != null ? info.role() : null;
    }

    /**
     * 현재 사용자가 OWNER 역할인지 확인
     *
     * @return OWNER이면 true
     */
    public static boolean isOwner() {
        return ROLE_OWNER.equals(getRole());
    }

    /**
     * 현재 사용자가 시스템 사용자인지 확인
     *
     * @return 시스템 사용자이면 true
     */
    public static boolean isSystemUser() {
        return SYSTEM_USER_ID.equals(getUserId());
    }

    /**
     * 시스템 작업용 컨텍스트 설정 (스케줄러, 배치 작업 등)
     *
     * @return AutoCloseable (try-with-resources 사용)
     */
    public static AutoCloseable withSystemContext() {
        return withContext(SYSTEM_TENANT_ID, SYSTEM_USER_ID, null, ROLE_OWNER);
    }

    /**
     * 현재 스레드의 테넌트 컨텍스트 정리
     */
    public static void clear() {
        ContextInfo info = CONTEXT.get();
        CONTEXT.remove();
        if (info != null) {
            log.debug("테넌트 컨텍스트 정리: tenantId={}, userId={}, dojangId={}",
                info.tenantId(), info.userId(), info.dojangId());
        }
    }
}
