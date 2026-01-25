package ch.ofte.symphony.jafts.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * Service for managing SEO metadata across pages
 * Use this service in your controllers to set SEO attributes dynamically
 */
@Service
public class SeoService {

    private static final String DEFAULT_TITLE = "Just A File Transfer Service - Simple & Secure File Sharing";
    private static final String DEFAULT_DESCRIPTION = "Simple, secure, and fast file sharing service. Share files easily with a unique key.";
    private static final String DEFAULT_KEYWORDS = "file transfer, file sharing, secure file transfer, file upload, file download, share files";
    private static final String DEFAULT_OG_IMAGE = "/images/og-image.png";

    /**
     * Sets default SEO attributes in the model
     *
     * @param model Spring MVC model
     */
    public void setDefaultSeo(Model model) {
        model.addAttribute("pageTitle", DEFAULT_TITLE);
        model.addAttribute("pageDescription", DEFAULT_DESCRIPTION);
        model.addAttribute("pageKeywords", DEFAULT_KEYWORDS);
        model.addAttribute("ogImage", DEFAULT_OG_IMAGE);
    }

    /**
     * Sets default SEO attributes in the model with canonical URL
     *
     * @param model Spring MVC model
     * @param request HTTP servlet request
     */
    public void setDefaultSeo(Model model, HttpServletRequest request) {
        setDefaultSeo(model);
        model.addAttribute("canonicalUrl", request.getRequestURL().toString());
    }

    /**
     * Sets custom SEO attributes in the model
     *
     * @param model Spring MVC model
     * @param title Page title
     * @param description Page description
     */
    public void setCustomSeo(Model model, String title, String description) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageDescription", description);
        model.addAttribute("pageKeywords", DEFAULT_KEYWORDS);
        model.addAttribute("ogImage", DEFAULT_OG_IMAGE);
    }

    /**
     * Sets custom SEO attributes in the model with canonical URL
     *
     * @param model Spring MVC model
     * @param request HTTP servlet request
     * @param title Page title
     * @param description Page description
     */
    public void setCustomSeo(Model model, HttpServletRequest request, String title, String description) {
        setCustomSeo(model, title, description);
        model.addAttribute("canonicalUrl", request.getRequestURL().toString());
    }

    /**
     * Sets full custom SEO attributes in the model
     *
     * @param model Spring MVC model
     * @param title Page title
     * @param description Page description
     * @param keywords Page keywords
     * @param ogImage Open Graph image URL
     */
    public void setFullSeo(Model model, String title, String description, String keywords, String ogImage) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageDescription", description);
        model.addAttribute("pageKeywords", keywords);
        model.addAttribute("ogImage", ogImage);
    }

    /**
     * Sets full custom SEO attributes in the model with canonical URL
     *
     * @param model Spring MVC model
     * @param request HTTP servlet request
     * @param title Page title
     * @param description Page description
     * @param keywords Page keywords
     * @param ogImage Open Graph image URL
     */
    public void setFullSeo(Model model, HttpServletRequest request, String title, String description, String keywords, String ogImage) {
        setFullSeo(model, title, description, keywords, ogImage);
        model.addAttribute("canonicalUrl", request.getRequestURL().toString());
    }

    /**
     * Sets canonical URL for the page
     *
     * @param model Spring MVC model
     * @param canonicalUrl Canonical URL
     */
    public void setCanonicalUrl(Model model, String canonicalUrl) {
        model.addAttribute("canonicalUrl", canonicalUrl);
    }
}

