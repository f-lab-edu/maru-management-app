package com.maru.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableCaching
public class CacheConfig {

    public static final String DOJANG_PERMISSIONS_CACHE = "dojangPermissions";
    public static final String TENANT_ACTIVE_CACHE = "tenantActive";
    public static final String DOJANG_ACTIVE_CACHE = "dojangActive";
    public static final String TENANT_OWNER_CACHE = "tenantOwner";
    public static final String EMPLOYMENT_CACHE = "employment";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(List.of(
            DOJANG_PERMISSIONS_CACHE,
            TENANT_ACTIVE_CACHE,
            DOJANG_ACTIVE_CACHE,
            TENANT_OWNER_CACHE,
            EMPLOYMENT_CACHE
        ));
        manager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats());
        return manager;
    }
}
