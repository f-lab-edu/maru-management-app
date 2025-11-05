package com.maru.security.cache;

import com.maru.security.PermissionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "redisPermissionCache")
public class DatabasePermissionCache implements PermissionCache {

    // TODO: PermissionRepository 생성 후 주입
    // private final PermissionRepository permissionRepository;

    @Override
    public boolean hasPermission(Long userId, Long tenantId, String resource, String action) {
        log.debug("권한 확인 - userId: {}, tenantId: {}, resource: {}, action: {}", userId, tenantId, resource, action);
        // TODO: PermissionRepository를 통한 실제 권한 조회 구현
        return false;
    }

    @Override
    public void invalidate(Long userId, Long tenantId) {
        // No-op: DatabasePermissionCache는 캐싱을 사용하지 않음
    }
}
