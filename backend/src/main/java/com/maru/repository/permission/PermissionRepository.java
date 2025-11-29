package com.maru.repository.permission;

import com.maru.domain.permission.Permission;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 사용자의 도장별 권한 목록 조회 (List-Based Caching)
     * tenantId: 데이터 격리, dojangId: 권한 격리
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID (Cross-Tenant 공격 방어)
     * @param dojangId 도장 ID (권한 격리 단위)
     * @return 부여된 권한 목록 (예: ["STUDENT:READ", "PAYMENT:WRITE"])
     */
    @Cacheable(value = "dojangPermissions", key = "'user:' + #userId + ':dojang:' + #dojangId")
    @Query("""
        SELECT CONCAT(p.resource, ':', p.action)
        FROM Permission p
        JOIN p.employment e
        WHERE e.user.id = :userId
          AND e.tenant.id = :tenantId
          AND e.dojang.id = :dojangId
          AND e.status = 'ACTIVE'
          AND p.granted = true
        """)
    Set<String> findGrantedPermissions(@Param("userId") Long userId,
                                       @Param("tenantId") Long tenantId,
                                       @Param("dojangId") Long dojangId);

    /**
     * 사용자의 도장별 권한 캐시 무효화
     *
     * @param userId 사용자 ID
     * @param dojangId 도장 ID
     */
    @CacheEvict(value = "dojangPermissions", key = "'user:' + #userId + ':dojang:' + #dojangId")
    default void evictPermissionCache(@Param("userId") Long userId,
                                      @Param("dojangId") Long dojangId) {
    }
}
