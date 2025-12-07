package com.maru.security;

public interface PermissionCache {

    /**
     * 사용자가 특정 도장에서 리소스에 대한 액션을 수행할 권한이 있는지 확인
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID (데이터 격리)
     * @param dojangId 도장 ID (권한 격리)
     * @param resource 리소스 이름 (예: "STUDENT", "PAYMENT")
     * @param action 액션 이름 (예: "READ", "WRITE")
     * @return 권한이 있으면 true, 없으면 false
     */
    boolean hasPermission(Long userId, Long tenantId, Long dojangId, String resource, String action);

    /**
     * 특정 사용자의 도장별 권한 캐시 무효화
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     */
    void invalidate(Long userId, Long tenantId, Long dojangId);
}
