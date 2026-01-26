package ch.ofte.symphony.jafts.p2p_transfer;

import ch.ofte.symphony.jafts.idempotency.IdempotencyKeyGenerator;
import ch.ofte.symphony.jafts.idempotency.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * P2P File Transfer Controller with Idempotency support
 * Prevents duplicate file uploads when user double-clicks
 */
@RestController
@RequestMapping("/api/p2p")
@RequiredArgsConstructor
public class P2PTransferController {

    // Constants for response keys
    private static final String KEY_ERROR = "error";
    private static final String KEY_STATUS = "status";
    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_ERROR = "error";
    private static final String KEY_FILE_ID = "fileId";
    private static final String KEY_FILE_NAME = "fileName";
    private static final String KEY_FILE_SIZE = "fileSize";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final String KEY_NEW_IDEMPOTENCY = "newIdempotencyKey";

    private final IdempotencyService idempotencyService;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;
    private final ObjectMapper objectMapper;

    /**
     * Upload file with idempotency key protection
     * Key must match the one generated in session (like CSRF token)
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            HttpSession session) {

        // Validate idempotency key from session (like CSRF validation)
        if (idempotencyKey == null || !idempotencyKeyGenerator.isValid(idempotencyKey, session)) {
            return buildErrorResponse("Invalid or missing idempotency key", ResponseEntity.badRequest());
        }

        try {
            // Process file upload
            String fileId = UUID.randomUUID().toString();

            Map<String, Object> response = new HashMap<>();
            response.put(KEY_FILE_ID, fileId);
            response.put(KEY_FILE_NAME, file.getOriginalFilename());
            response.put(KEY_FILE_SIZE, file.getSize());
            response.put(KEY_CONTENT_TYPE, file.getContentType());
            response.put(KEY_STATUS, STATUS_SUCCESS);

            // Mark as completed in cache
            String responseBody = objectMapper.writeValueAsString(response);
            idempotencyService.markCompleted(idempotencyKey, responseBody);

            // Refresh idempotency key for next upload (like CSRF token refresh)
            String newKey = idempotencyKeyGenerator.refreshKey(session);
            response.put(KEY_NEW_IDEMPOTENCY, newKey);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Mark as failed (allow retry)
            idempotencyService.markFailed(idempotencyKey);
            return buildErrorResponse("Upload failed: " + e.getMessage(), ResponseEntity.internalServerError());
        }
    }

    /**
     * Build error response with consistent structure
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            String errorMessage,
            ResponseEntity.BodyBuilder responseBuilder) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(KEY_ERROR, errorMessage);
        errorResponse.put(KEY_STATUS, STATUS_ERROR);
        return responseBuilder.body(errorResponse);
    }
}
