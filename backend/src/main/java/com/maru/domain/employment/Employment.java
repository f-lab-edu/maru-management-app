package com.maru.domain.employment;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.employment.exception.EmploymentErrorCode;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.permission.converter.PermissionSetConverter;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
import com.maru.domain.user.User;
import com.maru.domain.user.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dojang_id", nullable = false)
    private Dojang dojang;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentStatus status = EmploymentStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime endedAt;

    @Convert(converter = PermissionSetConverter.class)
    @Column(columnDefinition = "json")
    private Set<PermissionType> permissions = new HashSet<>();

    private Employment(User user, Tenant tenant, Dojang dojang) {
        validateNotNull(user, tenant, dojang);
        validateTenantIntegrity(tenant, dojang);

        this.user = user;
        this.tenant = tenant;
        this.dojang = dojang;
        this.status = EmploymentStatus.PENDING;
        this.joinedAt = LocalDateTime.now();
    }

    public static Employment create(User user, Tenant tenant, Dojang dojang) {
        return new Employment(user, tenant, dojang);
    }

    public static Employment createForOwner(User owner, Tenant tenant, Dojang dojang) {
        Employment employment = new Employment(owner, tenant, dojang);
        employment.status = EmploymentStatus.ACTIVE;
        return employment;
    }

    public void approve() {
        if (this.status != EmploymentStatus.PENDING) {
            throw new BusinessException(EmploymentErrorCode.NOT_PENDING);
        }
        this.status = EmploymentStatus.ACTIVE;
    }

    public void reject() {
        if (this.status != EmploymentStatus.PENDING) {
            throw new BusinessException(EmploymentErrorCode.NOT_PENDING);
        }
        this.status = EmploymentStatus.REJECTED;
    }

    public void suspend() {
        if (this.status != EmploymentStatus.ACTIVE) {
            throw new BusinessException(EmploymentErrorCode.NOT_ACTIVE);
        }
        this.status = EmploymentStatus.SUSPENDED;
    }

    public void reactivate() {
        if (this.status != EmploymentStatus.SUSPENDED) {
            throw new BusinessException(EmploymentErrorCode.NOT_SUSPENDED);
        }
        this.status = EmploymentStatus.ACTIVE;
    }

    public void leave() {
        if (this.status != EmploymentStatus.ACTIVE && this.status != EmploymentStatus.SUSPENDED) {
            throw new BusinessException(EmploymentErrorCode.NOT_ACTIVE_OR_SUSPENDED);
        }
        this.status = EmploymentStatus.LEFT;
        this.endedAt = LocalDateTime.now();
    }

    public void rejoin() {
        if (this.status != EmploymentStatus.LEFT && this.status != EmploymentStatus.REJECTED) {
            throw new BusinessException(EmploymentErrorCode.NOT_LEFT_OR_REJECTED);
        }

        this.status = EmploymentStatus.PENDING;
        this.joinedAt = LocalDateTime.now();
        this.endedAt = null;
    }

    public void grantPermission(PermissionType permission) {
        this.permissions.add(permission);
    }

    public void revokePermission(PermissionType permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(PermissionType permission) {
        return this.permissions.contains(permission);
    }

    public UserRole resolveRole(Long userId) {
        boolean isOwner = this.dojang.getOwner().getId().equals(userId);
        return isOwner ? UserRole.OWNER : UserRole.INSTRUCTOR;
    }

    private void validateNotNull(User user, Tenant tenant, Dojang dojang) {
        DomainAssert.notNull(user, EmploymentErrorCode.USER_REQUIRED);
        DomainAssert.notNull(tenant, EmploymentErrorCode.TENANT_REQUIRED);
        DomainAssert.notNull(dojang, EmploymentErrorCode.DOJANG_REQUIRED);
    }

    private void validateTenantIntegrity(Tenant tenant, Dojang dojang) {
        if (!dojang.getTenant().getId().equals(tenant.getId())) {
            throw new BusinessException(EmploymentErrorCode.TENANT_MISMATCH);
        }
    }
}
