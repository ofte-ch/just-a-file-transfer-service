package ch.ofte.symphony.jafts.idempotency;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generate and manage idempotency keys per session
 * Similar to CSRF token mechanism
 */
@Component
public class IdempotencyKeyGenerator {

    private static final String SESSION_KEY_ATTRIBUTE = "IDEMPOTENCY_KEY";

    /**
     * Get or generate idempotency key for current session
     * Each session gets one unique key, refreshed on each page load
     */
    public String getOrGenerateKey(HttpSession session) {
        String key = (String) session.getAttribute(SESSION_KEY_ATTRIBUTE);

        // Generate new key if not exists or on page refresh
        if (key == null) {
            key = UUID.randomUUID().toString();
            session.setAttribute(SESSION_KEY_ATTRIBUTE, key);
        }

        return key;
    }

    /**
     * Validate if the provided key matches session key
     */
    public boolean isValid(String providedKey, HttpSession session) {
        String sessionKey = (String) session.getAttribute(SESSION_KEY_ATTRIBUTE);
        return sessionKey != null && sessionKey.equals(providedKey);
    }

    /**
     * Invalidate current key (after successful use or on new page load)
     */
    public void invalidate(HttpSession session) {
        session.removeAttribute(SESSION_KEY_ATTRIBUTE);
    }

    /**
     * Refresh key (generate new one for next request)
     */
    public String refreshKey(HttpSession session) {
        invalidate(session);
        return getOrGenerateKey(session);
    }
}
