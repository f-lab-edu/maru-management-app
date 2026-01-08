package com.maru.service.auth;

import com.maru.common.exception.AuthErrorCode;
import com.maru.common.exception.AuthException;
import com.maru.common.exception.BusinessException;
import com.maru.controller.auth.dto.TokenRes;
import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.domain.user.OAuthAccount;
import com.maru.domain.user.OAuthProvider;
import com.maru.domain.user.User;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.user.OAuthAccountRepository;
import com.maru.repository.user.UserRepository;
import com.maru.security.JwtClaims;
import com.maru.service.auth.dto.OAuthUserInfo;
import com.maru.common.util.JwtUtil;
import com.maru.service.employment.EmploymentQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {
    private static final String ROLE_PENDING = "PENDING";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final EmploymentRepository employmentRepository;
    private final DojangRepository dojangRepository;
    private final Map<OAuthProvider, OAuthService> oauthServices;

    public AuthService(
        JwtUtil jwtUtil,
        UserRepository userRepository,
        OAuthAccountRepository oauthAccountRepository,
        EmploymentRepository employmentRepository,
        DojangRepository dojangRepository,
        List<OAuthService> oauthServiceList
    ) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.employmentRepository = employmentRepository;
        this.dojangRepository = dojangRepository;
        this.oauthServices = oauthServiceList.stream()
            .collect(Collectors.toMap(OAuthService::getProviderType, Function.identity()));
    }

    @Transactional(readOnly = true)
    public TokenRes refreshAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);
        JwtClaims claims = JwtClaims.fromJwt(jwtUtil.parseClaims(refreshToken));

        User user = findUserById(claims.userId());
        Employment employment = findActiveEmployment(claims.userId(), claims.dojangId());
        String tenantId = employment != null ? employment.getTenantId() : null;
        String dojangId = employment != null ? employment.getDojangId() : null;
        String role = resolveRole(user, employment, claims.role());

        return generateTokenResponse(user, tenantId, dojangId, role);
    }

    public String getAuthorizationUrl(OAuthProvider provider) {
        return getOAuthService(provider).getAuthorizationUrl();
    }

    @Transactional
    public TokenRes loginWithOAuth(OAuthProvider provider, String code) {
        OAuthUserInfo userInfo = getOAuthService(provider).authenticate(code);
        User user = createOrUpdateUserFromOAuth(userInfo);
        return generateTokenResponse(user);
    }

    @Transactional(readOnly = true)
    public TokenRes selectDojang(String userId, String dojangId) {
        User user = findUserById(userId);
        Employment employment = findActiveEmployment(userId, dojangId);

        String tenantId = employment.getTenantId();
        String resolvedDojangId = employment.getDojangId();
        String role = resolveRole(user, employment, null);

        return generateTokenResponse(user, tenantId, resolvedDojangId, role);
    }

    private OAuthService getOAuthService(OAuthProvider provider) {
        OAuthService service = oauthServices.get(provider);
        if (service == null) {
            log.error("지원하지 않는 OAuth Provider: {}", provider);
            throw new AuthException(AuthErrorCode.OAUTH_FAILED);
        }
        return service;
    }

    private void validateRefreshToken(String refreshToken) {
        JwtUtil.TokenValidationResult validationResult = jwtUtil.validateRefreshToken(refreshToken);

        if (validationResult == JwtUtil.TokenValidationResult.EXPIRED) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        } else if (validationResult != JwtUtil.TokenValidationResult.VALID) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_TOKEN));
    }

    private TokenRes generateTokenResponse(User user) {
        return generateTokenResponse(user, null, null, extractRoleString(user));
    }

    private TokenRes generateTokenResponse(User user, String tenantId, String dojangId, String role) {
        String accessToken = jwtUtil.generateAccessToken(
            user.getId(),
            tenantId,
            dojangId,
            role
        );

        String refreshToken = jwtUtil.generateRefreshToken(
            user.getId(),
            tenantId,
            dojangId,
            role
        );

        return TokenRes.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .tenantId(tenantId)
            .dojangId(dojangId)
            .role(role)
            .build();
    }

    private String extractRoleString(User user) {
        return user.getRole() != null ? user.getRole().name() : ROLE_PENDING;
    }

    private Employment findActiveEmployment(String userId, String dojangId) {
        if (dojangId == null) {
            return null;
        }

        return employmentRepository.findByUserIdAndDojangIdAndStatus(userId, dojangId, EmploymentStatus.ACTIVE)
            .orElseThrow(() -> new AuthException(AuthErrorCode.ACCESS_DENIED));
    }

    private String resolveRole(User user, Employment employment, String fallbackRole) {
        if (employment != null) {
            String ownerId = dojangRepository.findOwnerIdById(employment.getDojangId())
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));
            return EmploymentQueryService.resolveRole(user.getId(), ownerId).name();
        }

        if (fallbackRole != null) {
            return fallbackRole;
        }

        return extractRoleString(user);
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
