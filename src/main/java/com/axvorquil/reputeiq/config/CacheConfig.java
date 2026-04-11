package com.axvorquil.reputeiq.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            build("profiles",     120),   // 2 min — profile lookup
            build("score",         60),   // 1 min — reputation score
            build("achievements", 120),   // 2 min — achievement list
            build("endorsements",  60)    // 1 min — endorsement list
        ));
        return manager;
    }

    private CaffeineCache build(String name, int ttlSeconds) {
        return new CaffeineCache(name, Caffeine.newBuilder()
            .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
            .maximumSize(1000)
            .build());
    }
}
