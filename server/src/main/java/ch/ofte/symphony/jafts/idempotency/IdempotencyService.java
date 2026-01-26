package ch.ofte.symphony.jafts.idempotency;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service to manage idempotency keys using Caffeine cache
 * Prevents duplicate file uploads by tracking request states
 */
@Service
public class IdempotencyService {

    private final Cache<String, IdempotencyState> cache;

    public IdempotencyService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(2))
                .maximumSize(10_000)
                .build();
    }

    /**
     * Check the current state of an idempotency key
     */
    public IdempotencyState checkState(String key) {
        IdempotencyState state = cache.getIfPresent(key);
        if (state == null) {
            return new IdempotencyState(IdempotencyState.Status.NOT_FOUND);
        }
        return state;
    }

    /**
     * Mark a request as being processed
     */
    public void markProcessing(String key) {
        cache.put(key, new IdempotencyState(IdempotencyState.Status.PROCESSING));
    }

    /**
     * Mark a request as completed and store the response
     */
    public void markCompleted(String key, String responseBody) {
        cache.put(key, new IdempotencyState(IdempotencyState.Status.COMPLETED, responseBody));
    }

    /**
     * Mark a request as failed (remove from cache to allow retry)
     */
    public void markFailed(String key) {
        cache.invalidate(key);
    }
}
