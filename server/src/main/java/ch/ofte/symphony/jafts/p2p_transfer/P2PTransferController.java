package ch.ofte.symphony.jafts.p2p_transfer;

import ch.ofte.symphony.jafts.idempotency.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
@Slf4j
public class P2PTransferController {

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    /**
     * Upload file with idempotency key protection
     * Client must send X-Idempotency-Key header
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        log.info("Upload request received - file: {}, size: {} bytes, idempotency: {}",
                file.getOriginalFilename(), file.getSize(), idempotencyKey);

        try {
            // Process file upload (mock implementation)
            String fileId = UUID.randomUUID().toString();

            // TODO: Implement actual file saving logic
            // String savedPath = fileService.saveFile(file);

            Map<String, Object> response = new HashMap<>();
            response.put("fileId", fileId);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("contentType", file.getContentType());
            response.put("status", "success");

            // Mark as completed in cache if idempotency key provided
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                String responseBody = objectMapper.writeValueAsString(response);
                idempotencyService.markCompleted(idempotencyKey, responseBody);
            }

            log.info("File uploaded successfully - fileId: {}", fileId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Upload failed", e);

            // Mark as failed (allow retry)
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyService.markFailed(idempotencyKey);
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Upload thất bại: " + e.getMessage());
            errorResponse.put("status", "error");

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get idempotency cache statistics
     */
    @GetMapping("/idempotency/stats")
    public ResponseEntity<Map<String, String>> getIdempotencyStats() {
        Map<String, String> stats = new HashMap<>();
        stats.put("cacheStats", idempotencyService.getStats());
        return ResponseEntity.ok(stats);
    }
}
