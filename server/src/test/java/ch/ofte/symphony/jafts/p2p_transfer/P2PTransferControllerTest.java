package ch.ofte.symphony.jafts.p2p_transfer;

import ch.ofte.symphony.jafts.idempotency.IdempotencyKeyGenerator;
import ch.ofte.symphony.jafts.idempotency.IdempotencyService;
import ch.ofte.symphony.jafts.idempotency.IdempotencyState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for P2PTransferController
 */
@WebMvcTest(P2PTransferController.class)
class P2PTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdempotencyService idempotencyService;

    @MockBean
    private IdempotencyKeyGenerator idempotencyKeyGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;
    private MockMultipartFile testFile;
    private static final String VALID_KEY = "valid-idempotency-key";
    private static final String NEW_KEY = "new-idempotency-key";

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        testFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );
        
        // Setup default mock behavior for IdempotencyService (used by IdempotencyFilter)
        when(idempotencyService.checkState(anyString()))
                .thenReturn(new IdempotencyState(IdempotencyState.Status.NOT_FOUND));
    }

    @Test
    @WithMockUser
    void uploadFile_withValidKey_shouldReturnSuccess() throws Exception {
        // Setup mocks
        when(idempotencyKeyGenerator.isValid(eq(VALID_KEY), any())).thenReturn(true);
        when(idempotencyKeyGenerator.refreshKey(any())).thenReturn(NEW_KEY);

        mockMvc.perform(multipart("/api/p2p/upload")
                        .file(testFile)
                        .session(session)
                        .header("X-Idempotency-Key", VALID_KEY)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.fileName").value("test.txt"))
                .andExpect(jsonPath("$.fileId").exists())
                .andExpect(jsonPath("$.newIdempotencyKey").value(NEW_KEY));

        verify(idempotencyService).markCompleted(eq(VALID_KEY), anyString());
        verify(idempotencyKeyGenerator).refreshKey(any());
    }

    @Test
    @WithMockUser
    void uploadFile_withMissingKey_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/p2p/upload")
                        .file(testFile)
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Invalid or missing idempotency key"));
    }

    @Test
    @WithMockUser
    void uploadFile_withInvalidKey_shouldReturnBadRequest() throws Exception {
        when(idempotencyKeyGenerator.isValid(eq("invalid-key"), any())).thenReturn(false);

        mockMvc.perform(multipart("/api/p2p/upload")
                        .file(testFile)
                        .session(session)
                        .header("X-Idempotency-Key", "invalid-key")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Invalid or missing idempotency key"));
    }

    @Test
    @WithMockUser
    void uploadFile_shouldIncludeFileMetadata() throws Exception {
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                new byte[1024]
        );
        
        when(idempotencyKeyGenerator.isValid(eq(VALID_KEY), any())).thenReturn(true);
        when(idempotencyKeyGenerator.refreshKey(any())).thenReturn(NEW_KEY);

        mockMvc.perform(multipart("/api/p2p/upload")
                        .file(largeFile)
                        .session(session)
                        .header("X-Idempotency-Key", VALID_KEY)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("document.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(1024));
    }
}
