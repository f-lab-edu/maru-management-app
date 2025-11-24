package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.service.auth.dto.KakaoTokenRes;
import com.maru.service.auth.dto.KakaoUserInfoRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class KakaoOAuthService {

    private static final String AUTHORIZATION_URI = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Kakao OAuth Authorization URL 생성
     *
     * @return Kakao OAuth 인증 URL
     */
    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromUriString(AUTHORIZATION_URI)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .toUriString();
    }

    /**
     * Authorization Code를 Access Token으로 교환
     *
     * @param code Authorization Code
     * @return Kakao 토큰 정보
     */
    public KakaoTokenRes exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenRes> response = restTemplate.postForEntity(
                    TOKEN_URI,
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
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoRes> response = restTemplate.exchange(
                    USER_INFO_URI,
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
}
