package com.maru.domain.employment;

import com.maru.domain.common.BaseEntity;
import com.maru.domain.permission.converter.PermissionSetConverter;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
import com.maru.domain.user.User;
import com.maru.domain.user.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "employment",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_employment_user_dojang",
            columnNames = {"user_id", "dojang_id"}
        )
    },
    indexes = {
        @Index(name = "idx_employment_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_employment_dojang_status", columnList = "dojang_id, status")
    }
)
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

    @Column
    private LocalDateTime endedAt;

    @Convert(converter = PermissionSetConverter.class)
    @Column(name = "permissions", columnDefinition = "json")
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
            throw new IllegalStateException("대기 상태의 고용만 승인할 수 있습니다");
        }
        this.status = EmploymentStatus.ACTIVE;
    }

    public void reject() {
        if (this.status != EmploymentStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 고용만 거부할 수 있습니다");
        }
        this.status = EmploymentStatus.REJECTED;
    }

    public void suspend() {
        if (this.status != EmploymentStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 고용만 정지할 수 있습니다");
        }
        this.status = EmploymentStatus.SUSPENDED;
    }

    public void reactivate() {
        if (this.status != EmploymentStatus.SUSPENDED) {
            throw new IllegalStateException("정지 상태의 고용만 재활성화할 수 있습니다");
        }
        this.status = EmploymentStatus.ACTIVE;
    }

    public void leave() {
        if (this.status != EmploymentStatus.ACTIVE && this.status != EmploymentStatus.SUSPENDED) {
            throw new IllegalStateException("활성 또는 정지 상태의 고용만 퇴사 처리할 수 있습니다");
        }
        this.status = EmploymentStatus.LEFT;
        this.endedAt = LocalDateTime.now();
    }

    public void rejoin() {
        if (this.status != EmploymentStatus.LEFT && this.status != EmploymentStatus.REJECTED) {
            throw new IllegalStateException("퇴사 또는 거절 상태의 고용만 재입사 처리할 수 있습니다");
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

    private void validateNotNull(User user, Tenant tenant, Dojang dojang){
        Assert.notNull(user, "user는 필수입니다.");
        Assert.notNull(tenant, "tenant는 필수입니다.");
        Assert.notNull(dojang, "dojang은 필수입니다.");
    }

    private void validateTenantIntegrity(Tenant tenant, Dojang dojang){
        if(!dojang.getTenant().getId().equals(tenant.getId())){
            throw new IllegalStateException("도장의 Tenant와 입력된 Tenant가 일치하지 않습니다.");
        }
    }
}
