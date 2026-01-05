package com.maru.domain.tenant;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.tenant.exception.TenantErrorCode;
import com.maru.domain.user.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(unique = true, nullable = false, length = 8)
    private String slug;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Tenant(User owner, String slug) {
        validateNotNull(owner, slug);
        this.owner = owner;
        this.slug = slug;
        this.isActive = true;
    }

    public static Tenant create(User owner) {
        String slug = generateSlug();
        return new Tenant(owner, slug);
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

    private void validateNotNull(User owner, String slug) {
        DomainAssert.notNull(owner, TenantErrorCode.OWNER_REQUIRED);
        DomainAssert.hasText(slug, TenantErrorCode.SLUG_REQUIRED);
    }
}
