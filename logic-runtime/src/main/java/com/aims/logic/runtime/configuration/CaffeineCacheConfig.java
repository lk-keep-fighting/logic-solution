package com.aims.logic.runtime.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

//@Configuration
public class CaffeineCacheConfig {
//    @Bean
//    public Cache caffeineCache() {
//        return Caffeine.newBuilder()
//                .expireAfterAccess(1, TimeUnit.DAYS)
//                //初始容量为100
//                .initialCapacity(100)
//                //最大容量为200
//                .maximumSize(200)
//                .build();
//    }
}
