package com.maru.repository.employment;

import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.permission.PermissionType;
import com.maru.repository.employment.view.EmploymentDetailView;
import com.maru.repository.employment.view.InstructorView;
import com.maru.repository.employment.view.InstructorWithPermissionsView;
import com.maru.repository.employment.view.MyDojangView;
import com.maru.repository.employment.view.RecentApprovalView;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, String> {

    Optional<Employment> findByUserIdAndDojangId(String userId, String dojangId);

    List<Employment> findByDojangIdAndStatus(String dojangId, EmploymentStatus status);

    List<Employment> findByUserId(String userId);

    List<Employment> findByUserIdAndStatus(String userId, EmploymentStatus status);

    Optional<Employment> findByUserIdAndDojangIdAndStatus(String userId, String dojangId, EmploymentStatus status);

    Optional<Employment> findByUserIdAndTenantIdAndDojangIdAndStatus(
            String userId, String tenantId, String dojangId, EmploymentStatus status);

    boolean existsByUserIdAndDojangIdAndStatus(String userId, String dojangId, EmploymentStatus status);

    @Query("""
        SELECT e.id AS id,
               e.userId AS userId,
               u.name AS userName,
               u.email AS userEmail,
               u.phone AS userPhone,
               e.dojangId AS dojangId,
               d.name AS dojangName,
               e.tenantId AS tenantId,
               e.status AS status,
               e.joinedAt AS joinedAt,
               e.endedAt AS endedAt,
               e.createdAt AS createdAt
        FROM Employment e
        JOIN User u ON e.userId = u.id
        JOIN Dojang d ON e.dojangId = d.id
        WHERE e.dojangId = :dojangId
          AND e.status = :status
        """)
    List<EmploymentDetailView> findViewByDojangIdAndStatus(@Param("dojangId") String dojangId,
                                                           @Param("status") EmploymentStatus status);

    @Query("""
        SELECT e.id AS id,
               e.userId AS userId,
               u.name AS userName,
               u.email AS userEmail,
               u.phone AS userPhone,
               e.dojangId AS dojangId,
               d.name AS dojangName,
               e.tenantId AS tenantId,
               e.status AS status,
               e.joinedAt AS joinedAt,
               e.endedAt AS endedAt,
               e.createdAt AS createdAt
        FROM Employment e
        JOIN User u ON e.userId = u.id
        JOIN Dojang d ON e.dojangId = d.id
        WHERE e.userId = :userId
        """)
    List<EmploymentDetailView> findViewByUserId(@Param("userId") String userId);

    @Query("""
        SELECT e.id AS id,
               e.userId AS userId,
               u.name AS userName,
               u.email AS userEmail,
               u.phone AS userPhone,
               e.dojangId AS dojangId,
               d.name AS dojangName,
               e.tenantId AS tenantId,
               e.status AS status,
               e.joinedAt AS joinedAt,
               e.endedAt AS endedAt,
               e.createdAt AS createdAt
        FROM Employment e
        JOIN User u ON e.userId = u.id
        JOIN Dojang d ON e.dojangId = d.id
        WHERE e.id = :id
        """)
    Optional<EmploymentDetailView> findViewById(@Param("id") String id);

    @Query("""
        SELECT e.dojangId AS dojangId,
               d.name AS dojangName,
               e.tenantId AS tenantId,
               d.ownerId AS dojangOwnerId,
               e.userId AS userId,
               e.permissions AS permissions
        FROM Employment e
        JOIN Dojang d ON e.dojangId = d.id
        WHERE e.userId = :userId
          AND e.status = :status
        """)
    List<MyDojangView> findMyDojangViewByUserIdAndStatus(@Param("userId") String userId,
                                                         @Param("status") EmploymentStatus status);

    @Query("""
        SELECT e.id AS id,
               e.userId AS userId,
               u.name AS userName,
               u.email AS userEmail,
               u.phone AS userPhone,
               CAST(e.status AS string) AS status,
               e.joinedAt AS joinedAt,
               CASE WHEN e.status = 'SUSPENDED' THEN e.endedAt ELSE NULL END AS suspendedAt
        FROM Employment e
        JOIN User u ON e.userId = u.id
        JOIN Dojang d ON e.dojangId = d.id
        WHERE e.tenantId = :tenantId
          AND e.dojangId = :dojangId
          AND e.status IN ('ACTIVE', 'SUSPENDED')
          AND e.userId != d.ownerId
        ORDER BY e.joinedAt DESC
        """)
    List<InstructorView> findInstructorsByDojangId(@Param("tenantId") String tenantId,
                                                    @Param("dojangId") String dojangId);

    @Query("""
        SELECT e.id AS id,
               e.userId AS userId,
               u.name AS userName,
               u.email AS userEmail,
               u.phone AS userPhone,
               CAST(e.status AS string) AS status,
               e.joinedAt AS joinedAt,
               CASE WHEN e.status = 'SUSPENDED' THEN e.endedAt ELSE NULL END AS suspendedAt,
               e.permissions AS permissions
        FROM Employment e
        JOIN User u ON e.userId = u.id
        WHERE e.tenantId = :tenantId
          AND e.id = :employmentId
        """)
    Optional<InstructorWithPermissionsView> findInstructorWithPermissionsById(
            @Param("tenantId") String tenantId,
            @Param("employmentId") String employmentId);

    @Query("""
        SELECT e.userId as userId, u.name as userName, e.joinedAt as approvedAt
        FROM Employment e
        JOIN User u ON e.userId = u.id
        WHERE e.tenantId = :tenantId
          AND e.dojangId = :dojangId
          AND e.status = 'ACTIVE'
          AND e.joinedAt >= :since
        ORDER BY e.joinedAt DESC
        """)
    List<RecentApprovalView> findRecentApprovals(
            @Param("tenantId") String tenantId,
            @Param("dojangId") String dojangId,
            @Param("since") LocalDateTime since);
}
