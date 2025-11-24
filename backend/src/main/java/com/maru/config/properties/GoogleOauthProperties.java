package com.maru.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOauthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String scope,
        Urls urls
) {
    public record Urls(
            String authorization,
            String token,
            String userInfo
    ){}
}
