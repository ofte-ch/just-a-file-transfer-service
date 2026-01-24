package ch.ofte.symphony.jafts.idempotency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the state of an idempotency key in the cache
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyState {

    private Status status;
    private String responseBody;

    public IdempotencyState(Status status) {
        this.status = status;
        this.responseBody = null;
    }

    public enum Status {
        NOT_FOUND, // Key not in cache yet
        PROCESSING, // Request is being processed
        COMPLETED // Request completed successfully
    }
}
