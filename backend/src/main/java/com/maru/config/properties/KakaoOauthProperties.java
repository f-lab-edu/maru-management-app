package com.maru.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOauthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        Urls urls
) {
    public record Urls(
            String authorization,
            String token,
            String userInfo
    ){}
}
