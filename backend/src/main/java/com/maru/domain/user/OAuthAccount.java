package com.maru.domain.user;

import com.maru.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "oauth_account",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_oauth_provider_account",
            columnNames = {"provider", "provider_account_id"}
        )
    },
    indexes = {
        @Index(name = "idx_oauth_user_id", columnList = "user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_account_id", nullable = false, length = 255)
    private String providerAccountId;

    public OAuthAccount(User user, OAuthProvider provider, String providerAccountId) {
        this.user = user;
        this.provider = provider;
        this.providerAccountId = providerAccountId;
    }

    public void changeUser(User newUser) {
        this.user = newUser;
    }
}
