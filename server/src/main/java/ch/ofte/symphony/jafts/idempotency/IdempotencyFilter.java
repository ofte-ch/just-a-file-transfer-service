package ch.ofte.symphony.jafts.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to intercept requests and check idempotency keys
 * Prevents duplicate file uploads when user double-clicks
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyService idempotencyService;

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);
        String path = request.getRequestURI();

        // Only apply to P2P upload endpoints
        if (!shouldApplyIdempotency(path) || idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }


        IdempotencyState state = idempotencyService.checkState(idempotencyKey);

        switch (state.getStatus()) {
            case PROCESSING:
                // Request is already being processed
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Request is being processed, please wait\"}");
                return;

            case COMPLETED:
                // Return cached response
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(state.getResponseBody());
                return;

            case NOT_FOUND:
                // First time seeing this key
                idempotencyService.markProcessing(idempotencyKey);
                break;
        }

        // Continue processing
        filterChain.doFilter(request, response);
    }

    /**
     * Determine if idempotency should be applied to this path
     */
    private boolean shouldApplyIdempotency(String path) {
        return path != null && path.startsWith("/api/p2p/upload");
    }
}
