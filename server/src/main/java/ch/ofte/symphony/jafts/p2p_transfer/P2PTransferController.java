package ch.ofte.symphony.jafts.p2p_transfer;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * P2P File Transfer Controller with CSRF + Session ID protection
 * Uses Spring Security's CSRF token and Session ID for security
 */
@Slf4j
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

    @Value("${app.upload.dir:uploads/p2p}")
    private String uploadDir;

    private final UploadDuplicationService uploadDuplicationService;
    private final MessageSource messageSource;

    /**
     * Upload file with CSRF + Session ID protection
     * CSRF token is automatically validated by Spring Security
     * Session ID is managed by Spring container
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        // Check for duplicate upload using session ID
        if (uploadDuplicationService.isUploading(session)) {
            String duplicateMsg = messageSource.getMessage("p2p.upload.duplicate", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(409).body(Map.of(KEY_ERROR, duplicateMsg, KEY_STATUS, STATUS_ERROR));
        }

        // Mark session as uploading
        uploadDuplicationService.markUploading(session);

        try {
            // Validate file
            if (file.isEmpty()) {
                String emptyFileMsg = messageSource.getMessage("p2p.upload.selectFile", null, LocaleContextHolder.getLocale());
                return buildErrorResponse(emptyFileMsg, ResponseEntity.badRequest());
            }

            // Generate unique file ID
            String fileId = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file to disk with unique name
            String savedFileName = fileId + fileExtension;
            Path targetPath = uploadPath.resolve(savedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded successfully: {} (ID: {}, Size: {} bytes)", originalFilename, fileId, file.getSize());

            // Build success response
            Map<String, Object> response = new HashMap<>();
            response.put(KEY_FILE_ID, fileId);
            response.put(KEY_FILE_NAME, file.getOriginalFilename());
            response.put(KEY_FILE_SIZE, file.getSize());
            response.put(KEY_CONTENT_TYPE, file.getContentType());
            response.put(KEY_STATUS, STATUS_SUCCESS);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            String errorMsg = messageSource.getMessage("p2p.upload.error",
                new Object[]{e.getMessage()}, LocaleContextHolder.getLocale());
            return buildErrorResponse(errorMsg, ResponseEntity.internalServerError());
        } finally {
            // Always clear upload status after processing
            uploadDuplicationService.clearUpload(session);
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
