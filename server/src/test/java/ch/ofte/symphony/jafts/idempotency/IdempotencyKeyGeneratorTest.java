package ch.ofte.symphony.jafts.idempotency;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdempotencyKeyGenerator
 */
class IdempotencyKeyGeneratorTest {

    private IdempotencyKeyGenerator keyGenerator;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        keyGenerator = new IdempotencyKeyGenerator();
        session = new MockHttpSession();
    }

    @Test
    void getOrGenerateKey_shouldGenerateNewKeyForNewSession() {
        String key = keyGenerator.getOrGenerateKey(session);
        
        assertNotNull(key);
        assertFalse(key.isEmpty());
    }

    @Test
    void getOrGenerateKey_shouldReturnSameKeyForSameSession() {
        String key1 = keyGenerator.getOrGenerateKey(session);
        String key2 = keyGenerator.getOrGenerateKey(session);
        
        assertEquals(key1, key2);
    }

    @Test
    void getOrGenerateKey_shouldGenerateDifferentKeysForDifferentSessions() {
        HttpSession session2 = new MockHttpSession();
        
        String key1 = keyGenerator.getOrGenerateKey(session);
        String key2 = keyGenerator.getOrGenerateKey(session2);
        
        assertNotEquals(key1, key2);
    }

    @Test
    void isValid_shouldReturnTrueForMatchingKey() {
        String key = keyGenerator.getOrGenerateKey(session);
        
        assertTrue(keyGenerator.isValid(key, session));
    }

    @Test
    void isValid_shouldReturnFalseForNonMatchingKey() {
        keyGenerator.getOrGenerateKey(session);
        
        assertFalse(keyGenerator.isValid("wrong-key", session));
    }

    @Test
    void isValid_shouldReturnFalseForNullKey() {
        keyGenerator.getOrGenerateKey(session);
        
        assertFalse(keyGenerator.isValid(null, session));
    }

    @Test
    void isValid_shouldReturnFalseForEmptySession() {
        assertFalse(keyGenerator.isValid("any-key", session));
    }

    @Test
    void invalidate_shouldRemoveKeyFromSession() {
        String key = keyGenerator.getOrGenerateKey(session);
        
        keyGenerator.invalidate(session);
        
        assertFalse(keyGenerator.isValid(key, session));
    }

    @Test
    void refreshKey_shouldGenerateNewKey() {
        String key1 = keyGenerator.getOrGenerateKey(session);
        String key2 = keyGenerator.refreshKey(session);
        
        assertNotEquals(key1, key2);
        assertTrue(keyGenerator.isValid(key2, session));
        assertFalse(keyGenerator.isValid(key1, session));
    }

    @Test
    void generatedKey_shouldBeValidUUID() {
        String key = keyGenerator.getOrGenerateKey(session);
        
        // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        assertTrue(key.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }
}
