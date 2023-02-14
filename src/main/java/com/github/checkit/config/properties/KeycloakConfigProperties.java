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
@ConfigurationProperties("keycloak")
public class KeycloakConfigProperties {

    /**
     * Keycloak properties.
     */

    private final String realmSeparator = "/realms/";

    @Nonnull
    private String realm;

    @Nonnull
    private String clientId;

    @Nonnull
    private String authUrl;

    @Nonnull
    private String realmKey;

    @Nonnull
    private String secret;

    public String getAuthUrlWithRealm() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(authUrl);
        if (stringBuilder.charAt(stringBuilder.length() - 1) == '/')
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(realmSeparator);
        stringBuilder.append(realm);
        return stringBuilder.toString();
    }
}
