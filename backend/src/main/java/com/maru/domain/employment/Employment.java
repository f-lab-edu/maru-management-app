package com.maru.domain.employment;

import com.maru.domain.common.BaseEntity;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
import com.maru.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "employments",
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

    private Employment(User user, Tenant tenant, Dojang dojang) {
        this.user = user;
        this.tenant = tenant;
        this.dojang = dojang;
        this.status = EmploymentStatus.PENDING;
    }

    public static Employment create(User user, Tenant tenant, Dojang dojang) {
        return new Employment(user, tenant, dojang);
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
    }
}
