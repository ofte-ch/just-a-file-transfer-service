package ch.ofte.symphony.jafts;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for SEO-related endpoints like robots.txt and sitemap.xml
 */
@Controller
public class SeoController {

    /**
     * Serves robots.txt for search engine crawlers
     * Allows all crawlers to access all pages and points to the sitemap
     */
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getRobotsTxt(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);

        return """
                User-agent: *
                Allow: /
                
                Sitemap: %s/sitemap.xml
                
                # Crawl-delay for bots (optional)
                Crawl-delay: 1
                """.formatted(baseUrl);
    }

    /**
     * Serves sitemap.xml for search engines to discover pages
     * Lists all public URLs with their change frequency and priority
     */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String getSitemapXml(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        String currentDate = java.time.LocalDate.now().toString();

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9
                        http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
                    <url>
                        <loc>%s/</loc>
                        <lastmod>%s</lastmod>
                        <changefreq>weekly</changefreq>
                        <priority>1.0</priority>
                    </url>
                </urlset>
                """.formatted(baseUrl, currentDate);
    }

    /**
     * Helper method to extract the base URL from the request
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        // Only append port if it's not the default for the scheme
        if ((scheme.equals("http") && serverPort != 80) ||
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }
}

