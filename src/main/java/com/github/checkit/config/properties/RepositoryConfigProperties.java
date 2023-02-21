package com.github.checkit.config.properties;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("repository")
public class RepositoryConfigProperties {

    /**
     * Repository configuration properties.
     */

    @Nonnull
    private String url;

    @Nonnull
    private String driver;

    @Nonnull
    private String language;

    @Nonnull
    private String userIdPrefix;
}
