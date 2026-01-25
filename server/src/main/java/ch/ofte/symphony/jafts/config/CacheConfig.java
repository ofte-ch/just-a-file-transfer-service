package ch.ofte.symphony.jafts.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration for Idempotency Key tracking
 * Prevents duplicate file uploads when user double-clicks upload button
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache manager for idempotency keys
     * - Stores request state (PROCESSING, COMPLETED)
     * - TTL: 2 hours (enough for retry scenarios)
     * - Max size: 10,000 entries
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("idempotencyCache");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(2, TimeUnit.HOURS)
                .recordStats();
    }
}
