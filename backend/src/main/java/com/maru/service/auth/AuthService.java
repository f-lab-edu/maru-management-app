package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.common.util.JwtUtil;
import com.maru.domain.user.OAuthAccount;
import com.maru.domain.user.OAuthProvider;
import com.maru.domain.user.User;
import com.maru.repository.user.OAuthAccountRepository;
import com.maru.repository.user.UserRepository;
import com.maru.service.auth.dto.GoogleTokenRes;
import com.maru.service.auth.dto.GoogleUserInfoRes;
import com.maru.service.auth.dto.TokenRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final GoogleOAuthService googleOAuthService;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;

    /**
     * 로그인
     */
    public TokenRes login() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Access Token 갱신
     */
    public TokenRes refreshAccessToken(String refreshToken) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Google OAuth Authorization URL 조회
     */
    public String getGoogleAuthorizationUrl() {
        return googleOAuthService.getAuthorizationUrl();
    }

    /**
     * Kakao OAuth Authorization URL 조회
     */
    public String getKakaoAuthorizationUrl() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * Google OAuth 로그인 처리
     *
     * @param code Authorization Code
     * @return 인증 결과
     */
    @Transactional
    public TokenRes loginWithGoogle(String code) {
        GoogleTokenRes tokenRes = googleOAuthService.exchangeCodeForToken(code);
        GoogleUserInfoRes userInfo = googleOAuthService.getUserInfo(tokenRes.accessToken());

        User user = createOrUpdateUserFromOAuth(
            OAuthProvider.GOOGLE,
            userInfo.id(),
            userInfo.email(),
            userInfo.name()
        );

        String accessToken = jwtUtil.generateAccessToken(
            user.getId(),
            null,
            null,
            user.getRole() != null ? user.getRole().name() : "PENDING"
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return TokenRes.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .role(user.getRole() != null ? user.getRole().name() : "PENDING")
            .build();
    }

    /**
     * Kakao OAuth 로그인 처리
     *
     * @param code Authorization Code
     * @return 인증 결과
     */
    public TokenRes loginWithKakao(String code) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    @Transactional
    private User createOrUpdateUserFromOAuth(
        OAuthProvider provider,
        String providerId,
        String email,
        String name
    ) {
        Optional<OAuthAccount> existingAccount = oauthAccountRepository
            .findByProviderAndProviderAccountId(provider, providerId);

        if (existingAccount.isPresent()) {
            User user = existingAccount.get().getUser();
            user.updateLastLoginAt();
            return userRepository.save(user);
        } else {
            User newUser = User.createWithoutRole(name, email, null);
            User savedUser = userRepository.save(newUser);

            OAuthAccount newAccount = new OAuthAccount(savedUser, provider, providerId);
            oauthAccountRepository.save(newAccount);

            return savedUser;
        }
    }
}
