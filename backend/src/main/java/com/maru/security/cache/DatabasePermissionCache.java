package com.maru.security.cache;

import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.permission.PermissionType;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.security.PermissionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabasePermissionCache implements PermissionCache {

    private final EmploymentRepository employmentRepository;

    @Override
    public boolean hasPermission(Long userId, Long tenantId, Long dojangId, String resource, String action) {
        try {
            PermissionType requiredPermission = PermissionType.of(resource, action);

            Set<PermissionType> permissions = employmentRepository.findPermissions(
                    userId, tenantId, dojangId, EmploymentStatus.ACTIVE);

            if (permissions == null || permissions.isEmpty()) {
                return false;
            }

            boolean hasPermission = permissions.contains(requiredPermission);
            log.debug("권한 확인 - userId: {}, dojangId: {}, permission: {}, result: {}",
                    userId, dojangId, requiredPermission, hasPermission);
            return hasPermission;
        } catch (IllegalArgumentException e) {
            log.warn("존재하지 않는 권한 타입 요청: {}:{}", resource, action);
            return false;
        }
    }

    @Override
    public void invalidate(Long userId, Long tenantId, Long dojangId) {
        employmentRepository.evictPermissionCache(userId, dojangId);
        log.info("권한 캐시 무효화 - userId: {}, dojangId: {}", userId, dojangId);
    }
}
