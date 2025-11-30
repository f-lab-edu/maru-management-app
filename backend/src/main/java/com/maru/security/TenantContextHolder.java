package com.maru.security;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_CONTEXT = new ThreadLocal<>();

    /**
     * 현재 스레드의 테넌트 ID 설정
     *
     * @param tenantId 테넌트 ID
     */
    public static AutoCloseable withTenant(Long tenantId) {
        if(TENANT_CONTEXT.get() != null){
            throw new IllegalStateException("테넌트 컨텍스트가 이미 설정되어 있음.");
        }

        if(tenantId != null){
            TENANT_CONTEXT.set(tenantId);
            log.debug("테넌트 컨텍스트 설정: tenantId={}", tenantId);
        }
        return TenantContextHolder::clear;
    }

    /**Ï
     * 현재 스레드의 테넌트 ID 조회
     *
     * @return 테넌트 ID (설정되지 않았으면 null)
     */
    public static Long getTenantId() {
        Long tenantId = TENANT_CONTEXT.get();
        log.trace("테넌트 컨텍스트 조회: tenantId={}", tenantId);
        return tenantId;
    }

    /**
     * 현재 스레드의 테넌트 컨텍스트 정리
     */
    public static void clear() {
        Long tenantId = TENANT_CONTEXT.get();
        TENANT_CONTEXT.remove();
        log.debug("테넌트 컨텍스트 정리: tenantId={}", tenantId);
    }
}
