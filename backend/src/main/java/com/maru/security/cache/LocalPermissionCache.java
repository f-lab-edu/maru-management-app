package com.maru.security.cache;

import com.maru.config.CacheConfig;
import com.maru.domain.employment.Employment;
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

/**
 * 로컬 메모리 기반 권한 캐시 구현체
 * Caffeine 캐시를 사용하여 DB 조회 결과를 로컬 메모리에 캐싱
 *
 * @see PermissionCache
 * @see CacheConfig#DOJANG_PERMISSIONS_CACHE
 */
@Slf4j
@Component
public class LocalPermissionCache implements PermissionCache {

    private final EmploymentRepository employmentRepository;
    private final Cache cache;

    public LocalPermissionCache(EmploymentRepository employmentRepository,
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
    public boolean hasPermission(String userId, String tenantId, String dojangId, String resource, String action) {
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

    private Set<PermissionType> getPermissionsFromCache(String userId, String tenantId, String dojangId) {
        String key = buildKey(tenantId, userId, dojangId);

        Set<PermissionType> result = cache.get(key, () -> {
            log.debug("캐시 미스, DB 조회: {}", key);
            return employmentRepository.findByUserIdAndTenantIdAndDojangIdAndStatus(
                            userId, tenantId, dojangId, EmploymentStatus.ACTIVE)
                    .map(Employment::getPermissions)
                    .orElse(Set.of());
        });

        return Optional.ofNullable(result).orElse(Set.of());
    }

    @Override
    public void invalidate(String userId, String tenantId, String dojangId) {
        String key = buildKey(tenantId, userId, dojangId);
        cache.evict(key);
        log.info("권한 캐시 무효화 - {}", key);
    }

    private String buildKey(String tenantId, String userId, String dojangId) {
        return "tenant:" + tenantId + ":user:" + userId + ":dojang:" + dojangId;
    }
}
