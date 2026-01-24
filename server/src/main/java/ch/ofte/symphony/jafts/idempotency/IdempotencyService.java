package ch.ofte.symphony.jafts.idempotency;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service to manage idempotency keys using Caffeine cache
 * Prevents duplicate file uploads by tracking request states
 */
@Service
@Slf4j
public class IdempotencyService {

    private final Cache<String, IdempotencyState> cache;

    public IdempotencyService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(2))
                .maximumSize(10_000)
                .build();

        log.info("IdempotencyService initialized with 2-hour TTL, max 10,000 entries");
    }

    /**
     * Check the current state of an idempotency key
     */
    public IdempotencyState checkState(String key) {
        IdempotencyState state = cache.getIfPresent(key);
        if (state == null) {
            return new IdempotencyState(IdempotencyState.Status.NOT_FOUND);
        }
        log.debug("Idempotency key {} is in state: {}", key, state.getStatus());
        return state;
    }

    /**
     * Mark a request as being processed
     */
    public void markProcessing(String key) {
        cache.put(key, new IdempotencyState(IdempotencyState.Status.PROCESSING));
        log.debug("Marked idempotency key {} as PROCESSING", key);
    }

    /**
     * Mark a request as completed and store the response
     */
    public void markCompleted(String key, String responseBody) {
        cache.put(key, new IdempotencyState(IdempotencyState.Status.COMPLETED, responseBody));
        log.debug("Marked idempotency key {} as COMPLETED", key);
    }

    /**
     * Mark a request as failed (remove from cache to allow retry)
     */
    public void markFailed(String key) {
        cache.invalidate(key);
        log.debug("Removed idempotency key {} from cache (failed request)", key);
    }

    /**
     * Get cache statistics
     */
    public String getStats() {
        var stats = cache.stats();
        return String.format("Cache stats - hitRate: %.2f%%, size: %d",
                stats.hitRate() * 100, cache.estimatedSize());
    }
}
