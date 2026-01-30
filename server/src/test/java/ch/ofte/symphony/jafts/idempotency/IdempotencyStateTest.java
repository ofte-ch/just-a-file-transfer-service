package ch.ofte.symphony.jafts.idempotency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdempotencyState
 */
class IdempotencyStateTest {

    @Test
    void constructor_withStatusOnly_shouldSetStatusAndNullResponse() {
        IdempotencyState state = new IdempotencyState(IdempotencyState.Status.PROCESSING);
        
        assertEquals(IdempotencyState.Status.PROCESSING, state.getStatus());
        assertNull(state.getResponseBody());
    }

    @Test
    void constructor_withStatusAndResponse_shouldSetBoth() {
        String response = "{\"key\": \"value\"}";
        IdempotencyState state = new IdempotencyState(IdempotencyState.Status.COMPLETED, response);
        
        assertEquals(IdempotencyState.Status.COMPLETED, state.getStatus());
        assertEquals(response, state.getResponseBody());
    }

    @Test
    void noArgConstructor_shouldWork() {
        IdempotencyState state = new IdempotencyState();
        
        assertNull(state.getStatus());
        assertNull(state.getResponseBody());
    }

    @Test
    void setters_shouldWork() {
        IdempotencyState state = new IdempotencyState();
        
        state.setStatus(IdempotencyState.Status.NOT_FOUND);
        state.setResponseBody("test");
        
        assertEquals(IdempotencyState.Status.NOT_FOUND, state.getStatus());
        assertEquals("test", state.getResponseBody());
    }

    @Test
    void statusEnum_shouldHaveAllValues() {
        IdempotencyState.Status[] values = IdempotencyState.Status.values();
        
        assertEquals(3, values.length);
        assertNotNull(IdempotencyState.Status.NOT_FOUND);
        assertNotNull(IdempotencyState.Status.PROCESSING);
        assertNotNull(IdempotencyState.Status.COMPLETED);
    }
}
