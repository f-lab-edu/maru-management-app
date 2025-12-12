package com.maru.repository.employment;

import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.user.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Long> {

    Optional<Employment> findByUserAndDojang(User user, Dojang dojang);

    /**
     * 사용자의 도장별 권한 목록 조회
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID (데이터 격리)
     * @param dojangId 도장 ID (권한 격리)
     * @return 권한 Set (없으면 빈 Set)
     */
    @Cacheable(value = "dojangPermissions", key = "'user:' + #userId + ':dojang:' + #dojangId")
    @Query("""
        SELECT e.permissions
        FROM Employment e
        WHERE e.user.id = :userId
          AND e.tenant.id = :tenantId
          AND e.dojang.id = :dojangId
          AND e.status = :status
        """)
    Set<PermissionType> findPermissions(@Param("userId") Long userId,
                                        @Param("tenantId") Long tenantId,
                                        @Param("dojangId") Long dojangId,
                                        @Param("status") EmploymentStatus status);

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
