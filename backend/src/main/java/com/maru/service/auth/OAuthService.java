package com.maru.service.auth;

import com.maru.domain.user.OAuthProvider;
import com.maru.service.auth.dto.OAuthUserInfo;

/**
 * OAuth 인증 서비스 인터페이스
 */
public interface OAuthService {

    /**
     * OAuth Provider 타입 반환
     *
     * @return OAuth Provider
     */
    OAuthProvider getProviderType();

    /**
     * OAuth Authorization URL 생성
     *
     * @return 인증 URL
     */
    String getAuthorizationUrl();

    /**
     * Authorization Code로 사용자 인증 및 정보 조회
     *
     * @param code Authorization Code
     * @return OAuth 사용자 정보
     */
    OAuthUserInfo authenticate(String code);
}
