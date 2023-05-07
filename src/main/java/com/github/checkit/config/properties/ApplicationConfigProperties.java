package com.github.checkit.config.properties;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("application")
public class ApplicationConfigProperties {

    /**
     * Application version.
     */
    private String version;
    private Boolean publishToSSP;
    @Nonnull
    private CommentProperties comment;
    @Nonnull
    private PublicationContextProperties publicationContext;
    @Nonnull
    private NotificationProperties notification;

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public Boolean getPublishToSSP() {
        if (Objects.isNull(publishToSSP)) {
            publishToSSP = false;
        }
        return publishToSSP;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties("publicationcontext")
    public static class PublicationContextProperties {
        @Nonnull
        private Integer pageSize;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties("notification")
    public static class NotificationProperties {
        @Nonnull
        private Integer pageSize;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties("comment")
    public static class CommentProperties {
        @Nonnull
        private Integer rejectionMinimalContentLength;
    }
}
