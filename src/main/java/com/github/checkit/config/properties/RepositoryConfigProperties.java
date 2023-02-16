package com.github.checkit.config.properties;

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
    private String url;

    private String driver;

    private String language;
}
