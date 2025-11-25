package com.maru.service.auth.dto;

import com.maru.domain.user.OAuthProvider;

/**
 * OAuth 사용자 정보 (Provider 공통)
 *
 * @param provider OAuth 제공자
 * @param providerId Provider의 사용자 고유 ID
 * @param email 이메일 (nullable - Kakao 비즈앱 아닌 경우 null)
 * @param name 이름/닉네임
 */
public record OAuthUserInfo(
    OAuthProvider provider,
    String providerId,
    String email,
    String name
) {}
