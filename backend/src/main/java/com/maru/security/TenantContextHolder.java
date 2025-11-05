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
    public static void setTenantId(Long tenantId) {
        if (tenantId == null) {
            log.warn("테넌트 ID가 null입니다. 설정을 건너뜁니다.");
            return;
        }
        TENANT_CONTEXT.set(tenantId);
        log.debug("테넌트 컨텍스트 설정: tenantId={}", tenantId);
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
