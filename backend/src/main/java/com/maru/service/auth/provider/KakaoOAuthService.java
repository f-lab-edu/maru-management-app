package com.maru.service.auth.provider;

import com.maru.common.exception.AuthErrorCode;
import com.maru.common.exception.AuthException;
import com.maru.config.properties.KakaoOauthProperties;
import com.maru.domain.user.OAuthProvider;
import com.maru.service.auth.OAuthService;
import com.maru.service.auth.dto.KakaoTokenRes;
import com.maru.service.auth.dto.KakaoUserInfoRes;
import com.maru.service.auth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    private static final String DEFAULT_KAKAO_NICKNAME = "카카오 사용자";

    private final KakaoOauthProperties kakaoConfig;
    private final RestTemplate restTemplate;

    @Override
    public OAuthProvider getProviderType() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public String getAuthorizationUrl() {
        return buildOAuthUrl(kakaoConfig.urls().authorization());
    }

    /**
     * Authorization Code로 Kakao 인증 및 사용자 정보 조회
     *
     * @param code Authorization Code
     * @return OAuth 사용자 정보
     * @throws AuthException AUTH_OAUTH_FAILED - 토큰 교환 실패
     * @throws AuthException AUTH_OAUTH_USER_INFO_FAILED - 사용자 정보 조회 실패
     */
    @Override
    public OAuthUserInfo authenticate(String code) {
        KakaoTokenRes tokenRes = exchangeCodeForToken(code);
        KakaoUserInfoRes userInfo = fetchUserInfo(tokenRes.accessToken());

        return OAuthUserInfo.builder()
                .provider(OAuthProvider.KAKAO)
                .providerId(String.valueOf(userInfo.id()))
                .email(extractEmail(userInfo))
                .name(extractNickname(userInfo))
                .build();
    }

    private String extractEmail(KakaoUserInfoRes userInfo) {
        KakaoUserInfoRes.KakaoAccount account = userInfo.kakaoAccount();
        return account != null ? account.email() : null;
    }

    private String extractNickname(KakaoUserInfoRes userInfo) {
        KakaoUserInfoRes.KakaoAccount account = userInfo.kakaoAccount();
        if (account != null && account.profile() != null) {
            return account.profile().nickname();
        }
        return DEFAULT_KAKAO_NICKNAME;
    }

    private String buildOAuthUrl(String baseUrl) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("client_id", kakaoConfig.clientId())
                .queryParam("redirect_uri", kakaoConfig.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", kakaoConfig.scope())
                .toUriString();
    }

    private KakaoTokenRes exchangeCodeForToken(String code) {
        HttpEntity<MultiValueMap<String, String>> request = buildTokenRequest(code);

        try {
            ResponseEntity<KakaoTokenRes> response = restTemplate.postForEntity(
                    kakaoConfig.urls().token(),
                    request,
                    KakaoTokenRes.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Kakao 토큰 교환 실패 (클라이언트 에러): status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(AuthErrorCode.OAUTH_INVALID_CODE);
        } catch (Exception e) {
            log.error("Kakao 토큰 교환 실패: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.OAUTH_FAILED);
        }
    }

    private KakaoUserInfoRes fetchUserInfo(String accessToken) {
        HttpEntity<Void> request = buildBearerRequest(accessToken);

        try {
            ResponseEntity<KakaoUserInfoRes> response = restTemplate.exchange(
                    kakaoConfig.urls().userInfo(),
                    HttpMethod.GET,
                    request,
                    KakaoUserInfoRes.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Kakao 사용자 정보 조회 실패: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.OAUTH_USER_INFO_FAILED);
        }
    }

    private HttpEntity<MultiValueMap<String, String>> buildTokenRequest(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", kakaoConfig.clientId());
        params.add("client_secret", kakaoConfig.clientSecret());
        params.add("redirect_uri", kakaoConfig.redirectUri());
        params.add("grant_type", "authorization_code");

        return new HttpEntity<>(params, headers);
    }

    private HttpEntity<Void> buildBearerRequest(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }
}
