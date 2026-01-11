package ch.ofte.symphony.jafts.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for Thymeleaf templates.
 * Can be used in templates via #{@thymeleafUtils.methodName()}
 */
@Component("thymeleafUtils")
public class ThymeleafUtils {

    /**
     * Check if the current URI matches the given path
     * @param path the path to check
     * @return "active" if matches, empty string otherwise
     */
    public String isActive(String path) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return "";
        String currentUri = request.getRequestURI();
        return currentUri.equals(path) ? "active" : "";
    }

    /**
     * Check if the current URI starts with the given prefix
     * @param prefix the prefix to check
     * @return "active" if matches, empty string otherwise
     */
    public String isActivePrefix(String prefix) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return "";
        String currentUri = request.getRequestURI();
        return currentUri.startsWith(prefix) ? "active" : "";
    }

    /**
     * Check if the current URI matches any of the given paths
     * @param paths variable number of paths to check
     * @return "active" if any matches, empty string otherwise
     */
    public String isActiveAny(String... paths) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return "";
        String currentUri = request.getRequestURI();
        for (String path : paths) {
            if (currentUri.equals(path)) {
                return "active";
            }
        }
        return "";
    }

    /**
     * Get the current HTTP request
     * @return current HttpServletRequest or null if not available
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}

