package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.config.properties.KakaoOauthProperties;
import com.maru.service.auth.dto.KakaoTokenRes;
import com.maru.service.auth.dto.KakaoUserInfoRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.maru.common.exception.ErrorCode.*;

/**
 * Kakao OAuth 인증 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final KakaoOauthProperties kakaoConfig;
    private final RestTemplate restTemplate;

    /**
     * Kakao OAuth Authorization URL 생성
     *
     * @return Kakao OAuth 인증 URL
     */
    public String getAuthorizationUrl() {
        return buildOAuthUrl(kakaoConfig.urls().authorization());
    }

    /**
     * Authorization Code를 Access Token으로 교환
     *
     * @param code Authorization Code
     * @return Kakao 토큰 정보
     */
    public KakaoTokenRes exchangeCodeForToken(String code) {
        HttpEntity<MultiValueMap<String, String>> request = buildTokenRequest(code);

        try {
            ResponseEntity<KakaoTokenRes> response = restTemplate.postForEntity(
                    kakaoConfig.urls().token(),
                    request,
                    KakaoTokenRes.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Kakao 토큰 교환 실패: {}", e.getMessage());
            throw new AuthException(AUTH_OAUTH_FAILED);
        }
    }

    /**
     * Access Token으로 사용자 정보 조회
     *
     * @param accessToken Kakao Access Token
     * @return Kakao 사용자 정보
     */
    public KakaoUserInfoRes getUserInfo(String accessToken) {
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
            throw new AuthException(AUTH_OAUTH_USER_INFO_FAILED);
        }
    }


    /**
     * OAuth Authorization URL 생성
     *
     * @param baseUrl OAuth Provider의 authorization endpoint
     * @return 생성된 OAuth URL
     */
    private String buildOAuthUrl(String baseUrl) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("client_id", kakaoConfig.clientId())
                .queryParam("redirect_uri", kakaoConfig.redirectUri())
                .queryParam("response_type", "code")
                .toUriString();
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
        params.add("client_id", kakaoConfig.clientId());
        params.add("client_secret", kakaoConfig.clientSecret());
        params.add("redirect_uri", kakaoConfig.redirectUri());
        params.add("grant_type", "authorization_code");

        return new HttpEntity<>(params, headers);
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
