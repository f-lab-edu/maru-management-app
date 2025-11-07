package com.maru.security.cache;

import com.maru.security.PermissionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabasePermissionCache implements PermissionCache {

    // TODO: PermissionRepository 생성 후 주입
    @Cacheable(
            value = "permissions",
            key = "{#userId, #tenantId, #resource, #action}",
            unless = "#result == false"
    )
    @Override
    public boolean hasPermission(Long userId, Long tenantId, String resource, String action) {
        log.debug("DB에서 권한 조회 - userId: {}, tenantId: {}, resource: {}, action: {}",
                  userId, tenantId, resource, action);

        // TODO: PermissionRepository를 통한 실제 권한 조회 구현

        return false;
    }

    @CacheEvict(value = "permissions", allEntries = true)
    @Override
    public void invalidate(Long userId, Long tenantId) {
        log.info("권한 캐시 무효화 - userId: {}, tenantId: {}", userId, tenantId);
    }
}
