package com.maru.repository.employment;

import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Long> {

    Optional<Employment> findByUserAndDojang(User user, Dojang dojang);

    boolean existsByUserIdAndDojangId(Long userId, Long dojangId);

    @Query("""
        SELECT e
        FROM Employment e
        JOIN FETCH e.user
        WHERE e.dojang.id = :dojangId
          AND e.status = :status
        """)
    List<Employment> findByDojangIdAndStatus(@Param("dojangId") Long dojangId,
                                             @Param("status") EmploymentStatus status);

    @Query("""
        SELECT e
        FROM Employment e
        JOIN FETCH e.dojang
        WHERE e.user.id = :userId
        """)
    List<Employment> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT e
        FROM Employment e
        JOIN FETCH e.dojang d
        JOIN FETCH d.owner
        JOIN FETCH e.tenant
        WHERE e.user.id = :userId
          AND e.status = :status
        """)
    List<Employment> findActiveWithDojangAndTenant(@Param("userId") Long userId,
                                                   @Param("status") EmploymentStatus status);

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

    @Query("""
        SELECT e FROM Employment e
        JOIN FETCH e.dojang d
        JOIN FETCH d.owner
        JOIN FETCH e.tenant
        WHERE e.user.id = :userId
          AND e.dojang.id = :dojangId
          AND e.status = :status
        """)
    Optional<Employment> findByUserIdAndDojangIdAndStatus(
        @Param("userId") Long userId,
        @Param("dojangId") Long dojangId,
        @Param("status") EmploymentStatus status);
}
