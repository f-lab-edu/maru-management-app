package com.maru.domain.tenant;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.tenant.exception.TenantErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tenant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private String ownerId;

    @Column(unique = true, nullable = false, length = 8)
    private String slug;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Tenant(String ownerId, String slug) {
        validateNotNull(ownerId, slug);
        this.ownerId = ownerId;
        this.slug = slug;
        this.isActive = true;
    }

    public static Tenant create(String ownerId) {
        String slug = generateSlug();
        return new Tenant(ownerId, slug);
    }

    private static String generateSlug() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    private void validateNotNull(String ownerId, String slug) {
        DomainAssert.hasText(ownerId, TenantErrorCode.OWNER_REQUIRED);
        DomainAssert.hasText(slug, TenantErrorCode.SLUG_REQUIRED);
    }
}
