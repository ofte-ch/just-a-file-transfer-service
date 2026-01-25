package ch.ofte.symphony.jafts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Configuration class for internationalization (i18n).
 * Enables support for multiple languages in the application.
 */
@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

    /**
     * Configure the locale resolver.
     * Uses session-based locale storage with English as default.
     *
     * @return LocaleResolver configured with default locale
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.forLanguageTag("vi-VN"));
        return slr;
    }

    /**
     * Configure the locale change interceptor.
     * Allows changing locale via 'lang' request parameter.
     * Example: ?lang=vi for Vietnamese, ?lang=en for English
     *
     * @return LocaleChangeInterceptor configured with parameter name
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Add the locale change interceptor to the interceptor registry.
     *
     * @param registry the InterceptorRegistry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}

