package ch.ofte.symphony.jafts.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdempotencyService
 */
class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService();
    }

    @Test
    void checkState_shouldReturnNotFoundForNewKey() {
        String key = UUID.randomUUID().toString();
        
        IdempotencyState state = idempotencyService.checkState(key);
        
        assertEquals(IdempotencyState.Status.NOT_FOUND, state.getStatus());
        assertNull(state.getResponseBody());
    }

    @Test
    void markProcessing_shouldSetStateToProcessing() {
        String key = UUID.randomUUID().toString();
        
        idempotencyService.markProcessing(key);
        IdempotencyState state = idempotencyService.checkState(key);
        
        assertEquals(IdempotencyState.Status.PROCESSING, state.getStatus());
    }

    @Test
    void markCompleted_shouldSetStateToCompletedWithResponse() {
        String key = UUID.randomUUID().toString();
        String responseBody = "{\"status\": \"success\"}";
        
        idempotencyService.markCompleted(key, responseBody);
        IdempotencyState state = idempotencyService.checkState(key);
        
        assertEquals(IdempotencyState.Status.COMPLETED, state.getStatus());
        assertEquals(responseBody, state.getResponseBody());
    }

    @Test
    void markFailed_shouldRemoveKeyFromCache() {
        String key = UUID.randomUUID().toString();
        
        idempotencyService.markProcessing(key);
        idempotencyService.markFailed(key);
        IdempotencyState state = idempotencyService.checkState(key);
        
        assertEquals(IdempotencyState.Status.NOT_FOUND, state.getStatus());
    }

    @Test
    void stateTransition_processingToCompleted() {
        String key = UUID.randomUUID().toString();
        String responseBody = "{\"fileId\": \"123\"}";
        
        // Start processing
        idempotencyService.markProcessing(key);
        assertEquals(IdempotencyState.Status.PROCESSING, idempotencyService.checkState(key).getStatus());
        
        // Complete
        idempotencyService.markCompleted(key, responseBody);
        IdempotencyState finalState = idempotencyService.checkState(key);
        assertEquals(IdempotencyState.Status.COMPLETED, finalState.getStatus());
        assertEquals(responseBody, finalState.getResponseBody());
    }

    @Test
    void differentKeys_shouldHaveIndependentStates() {
        String key1 = UUID.randomUUID().toString();
        String key2 = UUID.randomUUID().toString();
        
        idempotencyService.markProcessing(key1);
        idempotencyService.markCompleted(key2, "response2");
        
        assertEquals(IdempotencyState.Status.PROCESSING, idempotencyService.checkState(key1).getStatus());
        assertEquals(IdempotencyState.Status.COMPLETED, idempotencyService.checkState(key2).getStatus());
    }
}
