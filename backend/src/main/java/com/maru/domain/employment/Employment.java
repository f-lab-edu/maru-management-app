package com.maru.domain.employment;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.BaseEntity;
import com.maru.domain.employment.exception.EmploymentErrorCode;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.permission.converter.PermissionSetConverter;
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

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "dojang_id", nullable = false)
    private String dojangId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentStatus status = EmploymentStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime endedAt;

    @Convert(converter = PermissionSetConverter.class)
    @Column(columnDefinition = "json")
    private Set<PermissionType> permissions = new HashSet<>();

    private Employment(String userId, String tenantId, String dojangId) {
        validateNotNull(userId, tenantId, dojangId);

        this.userId = userId;
        this.tenantId = tenantId;
        this.dojangId = dojangId;
        this.status = EmploymentStatus.PENDING;
        this.joinedAt = LocalDateTime.now();
    }

    public static Employment create(String userId, String tenantId, String dojangId) {
        return new Employment(userId, tenantId, dojangId);
    }

    public static Employment createForOwner(String ownerId, String tenantId, String dojangId) {
        Employment employment = new Employment(ownerId, tenantId, dojangId);
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

    private void validateNotNull(String userId, String tenantId, String dojangId) {
        DomainAssert.notNull(userId, EmploymentErrorCode.USER_REQUIRED);
        DomainAssert.notNull(tenantId, EmploymentErrorCode.TENANT_REQUIRED);
        DomainAssert.notNull(dojangId, EmploymentErrorCode.DOJANG_REQUIRED);
    }
}
