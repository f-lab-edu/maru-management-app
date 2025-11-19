package com.maru.repository.user;

import com.maru.domain.user.OAuthAccount;
import com.maru.domain.user.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderAccountId(
        OAuthProvider provider,
        String providerAccountId
    );

    boolean existsByProviderAndProviderAccountId(
        OAuthProvider provider,
        String providerAccountId
    );
}
