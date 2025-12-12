package com.maru.security.cache;

import com.maru.config.CacheConfig;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.permission.PermissionType;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.security.PermissionCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class DatabasePermissionCache implements PermissionCache {

    private final EmploymentRepository employmentRepository;
    private final Cache cache;

    public DatabasePermissionCache(EmploymentRepository employmentRepository,
                                   CacheManager cacheManager) {
        this.employmentRepository = employmentRepository;
        Cache resolved = cacheManager.getCache(CacheConfig.DOJANG_PERMISSIONS_CACHE);
        if (resolved == null) {
            log.error("캐시 '{}'를 찾을 수 없음. CacheConfig 확인 필요", CacheConfig.DOJANG_PERMISSIONS_CACHE);
            throw new IllegalStateException("필수 캐시 미설정: " + CacheConfig.DOJANG_PERMISSIONS_CACHE);
        }
        this.cache = resolved;
    }

    @Override
    public boolean hasPermission(Long userId, Long tenantId, Long dojangId, String resource, String action) {
        try {
            PermissionType required = PermissionType.of(resource, action);
            Set<PermissionType> permissions = getPermissionsFromCache(userId, tenantId, dojangId);

            if (permissions.isEmpty()) {
                return false;
            }

            boolean result = permissions.contains(required);
            log.debug("권한 확인 - userId: {}, dojangId: {}, permission: {}, result: {}",
                    userId, dojangId, required, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.warn("존재하지 않는 권한 타입 요청: {}:{}", resource, action);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Set<PermissionType> getPermissionsFromCache(Long userId, Long tenantId, Long dojangId) {
        String key = buildKey(tenantId, userId, dojangId);

        Set<PermissionType> result = cache.get(key, () -> {
            log.debug("캐시 미스, DB 조회: {}", key);
            Set<PermissionType> permissions = employmentRepository.findPermissions(
                    userId, tenantId, dojangId, EmploymentStatus.ACTIVE);
            return permissions != null ? permissions : Set.of();
        });

        return Optional.ofNullable(result).orElse(Set.of());
    }

    @Override
    public void invalidate(Long userId, Long tenantId, Long dojangId) {
        String key = buildKey(tenantId, userId, dojangId);
        cache.evict(key);
        log.info("권한 캐시 무효화 - {}", key);
    }

    private String buildKey(Long tenantId, Long userId, Long dojangId) {
        return "tenant:" + tenantId + ":user:" + userId + ":dojang:" + dojangId;
    }
}
