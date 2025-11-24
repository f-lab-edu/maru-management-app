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
     *
     * @return 토큰 정보
     */
    public TokenRes login() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
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
            user.getRole() != null ? user.getRole().name() : "PENDING"
        );

        return TokenRes.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .role(user.getRole() != null ? user.getRole().name() : "PENDING")
            .build();
    }

    /**
     * Refresh Token 유효성 검증
     *
     * @param refreshToken Refresh Token
     */
    private void validateRefreshToken(String refreshToken) {
        JwtUtil.TokenValidationResult validationResult = jwtUtil.validateRefreshToken(refreshToken);

        if (validationResult == JwtUtil.TokenValidationResult.EXPIRED) {
            throw new AuthException(AUTH_REFRESH_TOKEN_EXPIRED);
        } else if (validationResult != JwtUtil.TokenValidationResult.VALID) {
            throw new AuthException(AUTH_REFRESH_TOKEN_INVALID);
        }
    }

    /**
     * 사용자 ID로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AuthException(AUTH_INVALID_TOKEN));
    }

    /**
     * Google OAuth Authorization URL 조회
     *
     * @return Google OAuth 인증 URL
     */
    public String getGoogleAuthorizationUrl() {
        return googleOAuthService.getAuthorizationUrl();
    }

    /**
     * Kakao OAuth Authorization URL 조회
     *
     * @return Kakao OAuth 인증 URL
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
        GoogleUserInfoRes userInfo = fetchGoogleUserInfo(code);
        User user = createOrUpdateUserFromOAuth(
            OAuthProvider.GOOGLE,
            userInfo.id(),
            userInfo.email(),
            userInfo.name()
        );
        return generateTokenResponse(user);
    }

    /**
     * Google 사용자 정보 조회
     *
     * @param code Authorization Code
     * @return Google 사용자 정보
     */
    private GoogleUserInfoRes fetchGoogleUserInfo(String code) {
        GoogleTokenRes tokenRes = googleOAuthService.exchangeCodeForToken(code);
        return googleOAuthService.getUserInfo(tokenRes.accessToken());
    }

    /**
     * 토큰 응답 생성
     *
     * @param user 사용자
     * @return 토큰 정보
     */
    private TokenRes generateTokenResponse(User user) {
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
     * @return 토큰 정보
     */
    public TokenRes loginWithKakao(String code) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    /**
     * OAuth 정보로 사용자 생성 또는 업데이트
     *
     * @param provider OAuth 제공자
     * @param providerId OAuth 제공자의 사용자 ID
     * @param email 이메일
     * @param name 이름
     * @return 생성 또는 업데이트된 사용자
     */
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
