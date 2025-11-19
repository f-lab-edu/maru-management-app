package com.maru.service.auth;

import com.maru.common.exception.AuthException;
import com.maru.service.auth.dto.GoogleTokenRes;
import com.maru.service.auth.dto.GoogleUserInfoRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
public class GoogleOAuthService {

    private static final String AUTHORIZATION_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String SCOPE = "openid email profile";

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Google OAuth Authorization URL 생성
     *
     * @return Google OAuth 인증 URL
     */
    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromUriString(AUTHORIZATION_URI)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .toUriString();
    }

    /**
     * Authorization Code를 Access Token으로 교환
     *
     * @param code Authorization Code
     * @return Google 토큰 정보
     */
    public GoogleTokenRes exchangeCodeForToken(String code) {
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
            ResponseEntity<GoogleTokenRes> response = restTemplate.postForEntity(
                    TOKEN_URI,
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
     * Access Token으로 사용자 정보 조회
     *
     * @param accessToken Google Access Token
     * @return Google 사용자 정보
     */
    public GoogleUserInfoRes getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfoRes> response = restTemplate.exchange(
                    USER_INFO_URI,
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
}
