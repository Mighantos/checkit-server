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
    private UserProperties user;

    @Nonnull
    private GestoringRequestProperties gestoringRequest;

    @Nonnull
    private CommentProperties comment;

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties("user")
    public static class UserProperties {

        @Nonnull
        private String idPrefix;

        @Nonnull
        private String context;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties("gestoringrequest")
    public static class GestoringRequestProperties {

        @Nonnull
        private String context;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties("comment")
    public static class CommentProperties {

        @Nonnull
        private String context;

        @Nonnull
        private int rejectionMinimalLength;
    }
}
