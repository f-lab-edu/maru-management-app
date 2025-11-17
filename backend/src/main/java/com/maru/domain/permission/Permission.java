package com.maru.domain.permission;

import com.maru.domain.common.BaseEntity;
import com.maru.domain.employment.Employment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "permissions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_permission_employment_resource_action",
            columnNames = {"employment_id", "resource", "action"}
        )
    },
    indexes = {
        @Index(name = "idx_permission_employment_id", columnList = "employment_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_id", nullable = false)
    private Employment employment;

    @Column(nullable = false, length = 50)
    private String resource;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(nullable = false)
    private Boolean granted = true;

    private Permission(Employment employment, String resource, String action, Boolean granted) {
        this.employment = employment;
        this.resource = resource;
        this.action = action;
        this.granted = granted;
    }

    public static Permission grant(Employment employment, String resource, String action) {
        return new Permission(employment, resource, action, true);
    }

    public static Permission deny(Employment employment, String resource, String action) {
        return new Permission(employment, resource, action, false);
    }

    public void grant() {
        this.granted = true;
    }

    public void deny() {
        this.granted = false;
    }

    public boolean isGranted() {
        return this.granted;
    }
}
