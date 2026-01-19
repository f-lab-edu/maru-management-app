package com.maru.domain.tenant;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.tenant.exception.DojangErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dojang")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dojang extends SoftDeletableEntity {

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private String ownerId;

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

    private Dojang(String tenantId, String ownerId, String name, String address, String phone) {
        validateNotNull(tenantId, ownerId, name);
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.name = name;
        this.plan = Plan.FREE;
        this.address = address;
        this.phone = phone;
        this.isActive = true;
    }

    public static Dojang create(String tenantId, String ownerId, String name, String address, String phone) {
        return new Dojang(tenantId, ownerId, name, address, phone);
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

    private void validateNotNull(String tenantId, String ownerId, String name) {
        DomainAssert.hasText(tenantId, DojangErrorCode.TENANT_REQUIRED);
        DomainAssert.hasText(ownerId, DojangErrorCode.OWNER_REQUIRED);
        DomainAssert.hasText(name, DojangErrorCode.NAME_REQUIRED);
    }

}
