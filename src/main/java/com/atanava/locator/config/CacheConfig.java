package com.atanava.locator.config;

import com.atanava.locator.cache.MultiThreadRatingCache;
import com.atanava.locator.cache.RatingCache;
import com.atanava.locator.cache.SingleThreadRatingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class CacheConfig<K, V> {

    private final Environment env;

    @Bean(initMethod = "initCleaner")
    @Profile("multi-thread")
    public MultiThreadRatingCache<K, V> multiThreadRatingCache() {
        MultiThreadRatingCache<K, V> cache = new MultiThreadRatingCache<>(new ConcurrentHashMap<>(),
                new ConcurrentLinkedDeque<>(),
                new AtomicReference<>(new Date()));
        setFields(cache);
        cache.setSleepTime(Long.parseLong(Objects.requireNonNull(env.getProperty("rating-cache.sleep"))));
        log.debug("Created MultiThreadRatingCache");

        return cache;
    }

    @Bean
    @Profile("single-thread")
    public SingleThreadRatingCache<K, V> singleThreadRatingCache() {
        SingleThreadRatingCache<K, V> cache = new SingleThreadRatingCache<>(new HashMap<>(),
                new LinkedList<>(),
                new AtomicReference<>(new Date()));
        setFields(cache);
        log.debug("Created SingleThreadRatingCache");

        return cache;
    }

    private void setFields(RatingCache<K, V> cache) {
        cache.setEntryLifeTime(Long.parseLong(Objects.requireNonNull(env.getProperty("rating-cache.lifetime"))));
        cache.setMinRating(Integer.parseInt(Objects.requireNonNull(env.getProperty("rating-cache.rating"))));
        cache.setBatchSize(Integer.parseInt(Objects.requireNonNull(env.getProperty("rating-cache.batchsize"))));
        cache.setUseGC(Boolean.parseBoolean(Objects.requireNonNull(env.getProperty("rating-cache.usegc"))));
        cache.setMemorySaving(Boolean.parseBoolean(Objects.requireNonNull(env.getProperty("rating-cache.savememory"))));
    }
}
