package com.github.checkit.config.properties;

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
@ConfigurationProperties("github")
public class GitHubConfigProperties {

    private Boolean publishToSSP;
    private String organization;
    private String repository;
    private String token;


    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public Boolean getPublishToSSP() {
        if (Objects.isNull(publishToSSP)) {
            publishToSSP = false;
        }
        return publishToSSP;
    }
}
