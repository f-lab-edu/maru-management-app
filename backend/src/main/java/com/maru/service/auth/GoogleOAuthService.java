package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.config.properties.GoogleOauthProperties;
import com.maru.domain.user.OAuthProvider;
import com.maru.service.auth.dto.GoogleTokenRes;
import com.maru.service.auth.dto.GoogleUserInfoRes;
import com.maru.service.auth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthService {

    private final GoogleOauthProperties googleConfig;
    private final RestTemplate restTemplate;

    @Override
    public OAuthProvider getProviderType() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public String getAuthorizationUrl() {
        return buildOAuthUrl(googleConfig.urls().authorization());
    }

    /**
     * Authorization Code로 Google 인증 및 사용자 정보 조회
     *
     * @param code Authorization Code
     * @return OAuth 사용자 정보
     * @throws AuthException AUTH_OAUTH_FAILED - 토큰 교환 실패
     * @throws AuthException AUTH_OAUTH_USER_INFO_FAILED - 사용자 정보 조회 실패
     */
    @Override
    public OAuthUserInfo authenticate(String code) {
        GoogleTokenRes tokenRes = exchangeCodeForToken(code);
        GoogleUserInfoRes userInfo = fetchUserInfo(tokenRes.accessToken());

        return OAuthUserInfo.builder()
                .provider(OAuthProvider.GOOGLE)
                .providerId(userInfo.id())
                .email(userInfo.email())
                .name(userInfo.name())
                .build();
    }

    private String buildOAuthUrl(String baseUrl) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("client_id", googleConfig.clientId())
                .queryParam("redirect_uri", googleConfig.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", googleConfig.scope())
                .toUriString();
    }

    private GoogleTokenRes exchangeCodeForToken(String code) {
        HttpEntity<MultiValueMap<String, String>> request = buildTokenRequest(code);

        try {
            ResponseEntity<GoogleTokenRes> response = restTemplate.postForEntity(
                    googleConfig.urls().token(),
                    request,
                    GoogleTokenRes.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Google 토큰 교환 실패: {}", e.getMessage());
            throw new AuthException(AUTH_OAUTH_FAILED);
        }
    }

    private GoogleUserInfoRes fetchUserInfo(String accessToken) {
        HttpEntity<Void> request = buildBearerRequest(accessToken);

        try {
            ResponseEntity<GoogleUserInfoRes> response = restTemplate.exchange(
                    googleConfig.urls().userInfo(),
                    HttpMethod.GET,
                    request,
                    GoogleUserInfoRes.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Google 사용자 정보 조회 실패: {}", e.getMessage());
            throw new AuthException(AUTH_OAUTH_USER_INFO_FAILED);
        }
    }

    private HttpEntity<MultiValueMap<String, String>> buildTokenRequest(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleConfig.clientId());
        params.add("client_secret", googleConfig.clientSecret());
        params.add("redirect_uri", googleConfig.redirectUri());
        params.add("grant_type", "authorization_code");

        return new HttpEntity<>(params, headers);
    }

    private HttpEntity<Void> buildBearerRequest(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }
}
