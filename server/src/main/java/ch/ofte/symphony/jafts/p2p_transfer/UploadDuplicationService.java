package ch.ofte.symphony.jafts.p2p_transfer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service to prevent duplicate uploads using Session ID
 * Works together with CSRF token for security
 */
@Service
public class UploadDuplicationService {

    private final Cache<String, Boolean> uploadCache;

    public UploadDuplicationService() {
        this.uploadCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(10_000)
                .build();
    }

    /**
     * Check if this session is currently uploading
     * @param session HTTP session
     * @return true if upload is in progress
     */
    public boolean isUploading(HttpSession session) {
        String key = buildKey(session);
        return uploadCache.getIfPresent(key) != null;
    }

    /**
     * Mark this session as uploading
     * @param session HTTP session
     */
    public void markUploading(HttpSession session) {
        String key = buildKey(session);
        uploadCache.put(key, Boolean.TRUE);
    }

    /**
     * Clear upload status for this session
     * @param session HTTP session
     */
    public void clearUpload(HttpSession session) {
        String key = buildKey(session);
        uploadCache.invalidate(key);
    }

    /**
     * Build cache key from session ID
     * Session ID is managed by Spring Security - secure and unique per user
     */
    private String buildKey(HttpSession session) {
        return "upload:" + session.getId();
    }
}
