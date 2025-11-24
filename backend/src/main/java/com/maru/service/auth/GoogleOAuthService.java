package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.config.properties.GoogleOauthProperties;
import com.maru.service.auth.dto.GoogleTokenRes;
import com.maru.service.auth.dto.GoogleUserInfoRes;
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
public class GoogleOAuthService {

    private final GoogleOauthProperties googleConfig;
    private final RestTemplate restTemplate;

    /**
     * Google OAuth Authorization URL 생성
     *
     * @return Google OAuth 인증 URL
     */
    public String getAuthorizationUrl() {
        return buildOAuthUrl(googleConfig.urls().authorization());
    }

    /**
     * OAuth Authorization URL 생성
     *
     * @param baseUrl OAuth Provider의 authorization endpoint
     * @return 생성된 OAuth URL
     */
    private String buildOAuthUrl(String baseUrl) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("client_id", googleConfig.clientId())
                .queryParam("redirect_uri", googleConfig.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", googleConfig.scope())
                .toUriString();
    }

    /**
     * Authorization Code를 Access Token으로 교환
     *
     * @param code Authorization Code
     * @return Google 토큰 정보
     */
    public GoogleTokenRes exchangeCodeForToken(String code) {
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

    /**
     * 토큰 교환 요청 생성
     *
     * @param code Authorization Code
     * @return HTTP 요청 엔티티
     */
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

    /**
     * Access Token으로 사용자 정보 조회
     *
     * @param accessToken Google Access Token
     * @return Google 사용자 정보
     */
    public GoogleUserInfoRes getUserInfo(String accessToken) {
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

    /**
     * Bearer Token 인증 요청 생성
     *
     * @param accessToken Access Token
     * @return HTTP 요청 엔티티
     */
    private HttpEntity<Void> buildBearerRequest(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }
}
