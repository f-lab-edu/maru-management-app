package com.maru.security;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContextHolder {

    private static final ThreadLocal<ContextInfo> CONTEXT = new ThreadLocal<>();
    public record ContextInfo(String tenantId, String userId, String dojangId) {}

    /**
     * 현재 스레드의 컨텍스트 설정 (tenantId, userId, dojangId)
     *
     * @param tenantId 테넌트 ID
     * @param userId 사용자 ID
     * @param dojangId 도장 ID
     * @return AutoCloseable (try-with-resources 사용)
     */
    public static AutoCloseable withContext(String tenantId, String userId, String dojangId) {
        if (CONTEXT.get() != null) {
            throw new IllegalStateException("테넌트 컨텍스트가 이미 설정되어 있음");
        }

        CONTEXT.set(new ContextInfo(tenantId, userId, dojangId));
        log.debug("테넌트 컨텍스트 설정: tenantId={}, userId={}, dojangId={}", tenantId, userId, dojangId);
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
        return withContext(info.tenantId(), info.userId(), info.dojangId());
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
