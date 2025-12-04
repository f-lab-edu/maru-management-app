package com.maru.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    public static final String DOJANG_PERMISSIONS_CACHE = "dojangPermissions";

    /**
     * CacheManager Bean 생성
     * ConcurrentMapCacheManager 사용 (TTL 없음, 메모리 기반)
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(DOJANG_PERMISSIONS_CACHE);
    }
}
