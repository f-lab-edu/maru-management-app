package com.maru.security;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.auth.AuthErrorCode;
import com.maru.config.CacheConfig;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.domain.tenant.exception.TenantErrorCode;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DojangAccessValidator {

    private final TenantRepository tenantRepository;
    private final DojangRepository dojangRepository;
    private final EmploymentRepository employmentRepository;

    /**
     * 도장 접근 권한 검증
     *
     * @param dojangId 도장 ID
     */
    public void validate(String dojangId) {
        String tenantId = TenantContextHolder.getTenantId();
        String userId = TenantContextHolder.getUserId();

        if (tenantId == null || userId == null) {
            throw new BusinessException(AuthErrorCode.REQUIRED);
        }

        // 1. 테넌트 활성화 확인
        if (!isTenantActiveCached(tenantId)) {
            log.warn("비활성 테넌트 접근 시도: tenantId={}, userId={}, dojangId={}",
                tenantId, userId, dojangId);
            throw new BusinessException(TenantErrorCode.INACTIVE);
        }

        // 2. 도장 활성화 확인
        if (!isDojangActiveCached(dojangId)) {
            log.warn("비활성 도장 접근 시도: tenantId={}, userId={}, dojangId={}",
                tenantId, userId, dojangId);
            throw new BusinessException(DojangErrorCode.INACTIVE);
        }

        // 3. 테넌트 오너면 통과 (모든 도장 접근 가능)
        if (isOwnerCached(tenantId, userId)) {
            log.debug("테넌트 오너 접근: tenantId={}, userId={}, dojangId={}",
                tenantId, userId, dojangId);
            return;
        }

        // 4. 도장 소속 확인
        if (!hasActiveEmploymentCached(userId, dojangId)) {
            log.warn("도장 접근 거부: tenantId={}, userId={}, dojangId={}",
                tenantId, userId, dojangId);
            throw new BusinessException(DojangErrorCode.UNAUTHORIZED_ACCESS);
        }

        log.debug("도장 소속자 접근: tenantId={}, userId={}, dojangId={}",
            tenantId, userId, dojangId);
    }

    @Cacheable(value = CacheConfig.TENANT_ACTIVE_CACHE, key = "#tenantId")
    public boolean isTenantActiveCached(String tenantId) {
        return tenantRepository.existsByIdAndIsActiveTrue(tenantId);
    }

    @Cacheable(value = CacheConfig.DOJANG_ACTIVE_CACHE, key = "#dojangId")
    public boolean isDojangActiveCached(String dojangId) {
        return dojangRepository.existsByIdAndIsActiveTrue(dojangId);
    }

    @Cacheable(value = CacheConfig.TENANT_OWNER_CACHE, key = "#tenantId + ':' + #userId")
    public boolean isOwnerCached(String tenantId, String userId) {
        return tenantRepository.existsByIdAndOwnerId(tenantId, userId);
    }

    @Cacheable(value = CacheConfig.EMPLOYMENT_CACHE, key = "#userId + ':' + #dojangId")
    public boolean hasActiveEmploymentCached(String userId, String dojangId) {
        return employmentRepository.existsByUserIdAndDojangIdAndStatus(
            userId, dojangId, EmploymentStatus.ACTIVE);
    }

    @CacheEvict(value = CacheConfig.TENANT_ACTIVE_CACHE, key = "#tenantId")
    public void evictTenantActiveCache(String tenantId) {
        log.info("테넌트 활성화 캐시 무효화: tenantId={}", tenantId);
    }

    @CacheEvict(value = CacheConfig.DOJANG_ACTIVE_CACHE, key = "#dojangId")
    public void evictDojangActiveCache(String dojangId) {
        log.info("도장 활성화 캐시 무효화: dojangId={}", dojangId);
    }

    @CacheEvict(value = CacheConfig.TENANT_OWNER_CACHE, key = "#tenantId + ':' + #userId")
    public void evictOwnerCache(String tenantId, String userId) {
        log.info("테넌트 오너 캐시 무효화: tenantId={}, userId={}", tenantId, userId);
    }

    @CacheEvict(value = CacheConfig.EMPLOYMENT_CACHE, key = "#userId + ':' + #dojangId")
    public void evictEmploymentCache(String userId, String dojangId) {
        log.info("Employment 캐시 무효화: userId={}, dojangId={}", userId, dojangId);
    }
}
