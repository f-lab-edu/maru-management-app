package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.common.util.JwtUtil;
import com.maru.domain.user.OAuthAccount;
import com.maru.domain.user.OAuthProvider;
import com.maru.domain.user.User;
import com.maru.repository.user.OAuthAccountRepository;
import com.maru.repository.user.UserRepository;
import com.maru.service.auth.dto.OAuthUserInfo;
import com.maru.controller.auth.dto.TokenRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
public class AuthService {
    private static final String ROLE_PENDING = "PENDING";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final Map<OAuthProvider, OAuthService> oauthServices;

    public AuthService(
        JwtUtil jwtUtil,
        UserRepository userRepository,
        OAuthAccountRepository oauthAccountRepository,
        List<OAuthService> oauthServiceList
    ) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.oauthServices = oauthServiceList.stream()
            .collect(Collectors.toMap(OAuthService::getProviderType, Function.identity()));
    }

    /**
     * Refresh Token으로 Access Token 갱신
     *
     * @param refreshToken Refresh Token
     * @return 갱신된 토큰 정보
     */
    @Transactional(readOnly = true)
    public TokenRes refreshAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);
        Long userId = jwtUtil.extractUserId(refreshToken);
        User user = findUserById(userId);

        String newAccessToken = jwtUtil.generateAccessToken(
            user.getId(),
            null,
            null,
            extractRoleString(user)
        );

        return TokenRes.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .role(extractRoleString(user))
            .build();
    }

    /**
     * OAuth Authorization URL 조회
     *
     * @param provider OAuth 제공자
     * @return OAuth 인증 URL
     */
    public String getAuthorizationUrl(OAuthProvider provider) {
        return getOAuthService(provider).getAuthorizationUrl();
    }

    /**
     * OAuth 로그인 처리
     *
     * @param provider OAuth 제공자
     * @param code Authorization Code
     * @return 토큰 정보
     */
    @Transactional
    public TokenRes loginWithOAuth(OAuthProvider provider, String code) {
        OAuthUserInfo userInfo = getOAuthService(provider).authenticate(code);
        User user = createOrUpdateUserFromOAuth(userInfo);
        return generateTokenResponse(user);
    }

    private OAuthService getOAuthService(OAuthProvider provider) {
        OAuthService service = oauthServices.get(provider);
        if (service == null) {
            log.error("지원하지 않는 OAuth Provider: {}", provider);
            throw new AuthException(AUTH_OAUTH_FAILED);
        }
        return service;
    }

    private void validateRefreshToken(String refreshToken) {
        JwtUtil.TokenValidationResult validationResult = jwtUtil.validateRefreshToken(refreshToken);

        if (validationResult == JwtUtil.TokenValidationResult.EXPIRED) {
            throw new AuthException(AUTH_REFRESH_TOKEN_EXPIRED);
        } else if (validationResult != JwtUtil.TokenValidationResult.VALID) {
            throw new AuthException(AUTH_REFRESH_TOKEN_INVALID);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AuthException(AUTH_INVALID_TOKEN));
    }

    private TokenRes generateTokenResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(
            user.getId(),
            null,
            null,
            extractRoleString(user)
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return TokenRes.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .role(extractRoleString(user))
            .build();
    }

    private String extractRoleString(User user) {
        return user.getRole() != null ? user.getRole().name() : ROLE_PENDING;
    }

    private User createOrUpdateUserFromOAuth(OAuthUserInfo userInfo) {
        Optional<OAuthAccount> existingAccount = oauthAccountRepository
            .findByProviderAndProviderAccountId(userInfo.provider(), userInfo.providerId());

        if (existingAccount.isPresent()) {
            User user = existingAccount.get().getUser();
            user.updateLastLoginAt();
            return userRepository.save(user);
        } else {
            User newUser = User.createWithoutRole(userInfo.name(), userInfo.email(), null);
            User savedUser = userRepository.save(newUser);

            OAuthAccount newAccount = new OAuthAccount(savedUser, userInfo.provider(), userInfo.providerId());
            oauthAccountRepository.save(newAccount);

            return savedUser;
        }
    }
}
