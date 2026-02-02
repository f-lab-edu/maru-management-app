package com.maru.repository.user;

import com.maru.domain.user.OAuthAccount;
import com.maru.domain.user.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, String> {

    Optional<OAuthAccount> findByProviderAndProviderAccountId(
        OAuthProvider provider,
        String providerAccountId
    );

    Optional<OAuthAccount> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
