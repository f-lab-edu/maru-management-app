package com.maru.security.cache;

import com.maru.repository.permission.PermissionRepository;
import com.maru.security.PermissionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabasePermissionCache implements PermissionCache {

    private final PermissionRepository permissionRepository;

    @Override
    public boolean hasPermission(Long userId, Long tenantId, Long dojangId, String resource, String action) {
        Set<String> permissions = permissionRepository.findGrantedPermissions(userId, tenantId, dojangId);
        String permissionKey = resource + ":" + action;

        boolean hasPermission = permissions.contains(permissionKey);

        if (log.isDebugEnabled()) {
            log.debug("권한 확인 - userId: {}, dojangId: {}, permission: {}, result: {}",
                    userId, dojangId, permissionKey, hasPermission);
        }

        return hasPermission;
    }

    @Override
    public void invalidate(Long userId, Long tenantId, Long dojangId) {
        permissionRepository.evictPermissionCache(userId, dojangId);
        log.info("권한 캐시 무효화 - userId: {}, dojangId: {}", userId, dojangId);
    }
}
