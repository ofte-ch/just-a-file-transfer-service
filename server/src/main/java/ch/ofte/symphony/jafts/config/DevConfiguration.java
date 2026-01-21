package ch.ofte.symphony.jafts.config;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * Credit: <a href="https://bootify.io/frontend/thymeleaf-templates-hot-reload.html">Thymeleaf templates hot reload guide</a>
 */
@Configuration
@Profile("dev")
public class DevConfiguration implements WebMvcConfigurer {
    private static final String APPLICATION_FILE_NAME = "application-dev.properties";
    private String sourceRootPath = null;

    DevConfiguration(final TemplateEngine templateEngine) throws IOException {
        final ClassPathResource applicationFile = new ClassPathResource(APPLICATION_FILE_NAME);
        if (applicationFile.isFile()) {
            File sourceRoot = applicationFile.getFile().getParentFile();
            while (sourceRoot != null
                    && sourceRoot.getParentFile() != null
                    && Objects.requireNonNull(sourceRoot.listFiles((dir, name) -> name.equals("mvnw"))).length != 1) {
                sourceRoot = sourceRoot.getParentFile();
            }
            if (sourceRoot == null) {
                return;
            }
            sourceRootPath = sourceRoot.getPath();
            final FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
            fileTemplateResolver.setPrefix(sourceRoot.getPath() + "/src/main/resources/templates/");
            fileTemplateResolver.setSuffix(".html");
            fileTemplateResolver.setCacheable(false);
            fileTemplateResolver.setCharacterEncoding("UTF-8");
            fileTemplateResolver.setCheckExistence(true);
            templateEngine.setTemplateResolver(fileTemplateResolver);
        }
    }

    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
        if (this.sourceRootPath != null) {
            String staticPath = "file:" + this.sourceRootPath + "/src/main/resources/static/";

            registry.addResourceHandler("/css/**")
                    .addResourceLocations(staticPath + "css/")
                    .setCachePeriod(0);

            registry.addResourceHandler("/js/**")
                    .addResourceLocations(staticPath + "js/")
                    .setCachePeriod(0);

            registry.addResourceHandler("/images/**")
                    .addResourceLocations(staticPath + "images/")
                    .setCachePeriod(0);

            registry.addResourceHandler("/assets/**")
                    .addResourceLocations(staticPath + "assets/")
                    .setCachePeriod(0);
        }
    }
}
