package com.maru.domain.tenant;

import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(
    name = "dojang",
    indexes = {
        @Index(name = "idx_dojang_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_dojang_user_id", columnList = "user_id"),
        @Index(name = "idx_dojang_plan", columnList = "plan")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dojang extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Plan plan = Plan.FREE;

    @Column(length = 500)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Dojang(Tenant tenant, User owner, String name, String address, String phone) {
        validateNotNull(tenant, owner, name);
        validateOwnership(tenant, owner);
        this.tenant = tenant;
        this.owner = owner;
        this.name = name;
        this.plan = Plan.FREE;
        this.address = address;
        this.phone = phone;
        this.isActive = true;
    }

    public static Dojang create(Tenant tenant, User owner, String name, String address, String phone) {
        return new Dojang(tenant, owner, name, address, phone);
    }

    public void updatePlan(Plan newPlan) {
        this.plan = newPlan;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateInfo(String name, String address, String phone) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.address = address;
        this.phone = phone;
    }

    private void validateNotNull(Tenant tenant, User owner, String name) {
        Assert.notNull(tenant, "tenant는 필수입니다.");
        Assert.notNull(owner, "owner는 필수입니다.");
        Assert.hasText(name, "name은 필수입니다.");
    }

    private void validateOwnership(Tenant tenant, User owner) {
        if (!tenant.getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("도장 소유자는 테넌트 소유자와 일치해야 합니다.");
        }
    }
}
