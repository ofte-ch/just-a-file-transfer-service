package ch.ofte.symphony.jafts.config;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * Credit: <a href="https://bootify.io/frontend/thymeleaf-templates-hot-reload.html">Thymeleaf templates hot reload guide</a>
 */
@Configuration
@Profile("dev")
public class DevConfiguration {
    private static final String APPLICATION_FILE_NAME = "application-dev.properties";

    DevConfiguration(final TemplateEngine templateEngine) throws IOException {
        final ClassPathResource applicationFile = new ClassPathResource(APPLICATION_FILE_NAME);
        if (applicationFile.isFile()) {
            File sourceRoot = applicationFile.getFile().getParentFile();
            while (Objects.requireNonNull(sourceRoot.listFiles((dir, name) -> name.equals("mvnw"))).length != 1) {
                sourceRoot = sourceRoot.getParentFile();
            }
            final FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
            fileTemplateResolver.setPrefix(sourceRoot.getPath() + "/src/main/resources/templates/");
            fileTemplateResolver.setSuffix(".html");
            fileTemplateResolver.setCacheable(false);
            fileTemplateResolver.setCharacterEncoding("UTF-8");
            fileTemplateResolver.setCheckExistence(true);
            templateEngine.setTemplateResolver(fileTemplateResolver);
        }
    }
}
