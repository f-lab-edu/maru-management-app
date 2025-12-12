package com.maru.service.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kakao 사용자 정보 응답 DTO
 */
public record KakaoUserInfoRes(
    Long id,
    @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    /**
     * Kakao 계정 정보
     */
    public record KakaoAccount(
        String email,
        Profile profile
    ) {
        /**
         * Kakao 프로필 정보
         */
        public record Profile(
            String nickname
        ) {}
    }
}
