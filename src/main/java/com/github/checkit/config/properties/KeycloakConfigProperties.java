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
    private final String protocolSeparator = "/protocol/openid-connect/";

    private final String authorizationSuffix = "auth";
    private final String tokenSuffix = "token";
    private final String userInfoSuffix = "userinfo";
    private final String jwksSuffix = "certs";
    private final String endSessionSuffix = "logout";

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

    private String issuerUrl;

    private String authorizationUrl;

    private String tokenUrl;

    private String userInfoUrl;

    private String jwksUrl;

    private String endSessionUrl;

    public String getIssuerUrl() {
        if (issuerUrl == null)
            generateIssuerUrl();
        return issuerUrl;
    }

    public String getJwksUrl() {
        if (jwksUrl == null)
            jwksUrl = generateProtocolPrefixUrl() + jwksSuffix;
        return jwksUrl;
    }

    public String getAuthorizationUrl() {
        if (authorizationUrl == null)
            authorizationUrl = generateProtocolPrefixUrl() + authorizationSuffix;
        return authorizationUrl;
    }

    public String getTokenUrl() {
        if (tokenUrl == null)
            tokenUrl = generateProtocolPrefixUrl() + tokenSuffix;
        return tokenUrl;
    }

    public String getUserInfoUrl() {
        if (userInfoUrl == null)
            userInfoUrl = generateProtocolPrefixUrl() + userInfoSuffix;
        return userInfoUrl;
    }

    public String getEndSessionUrl() {
        if (endSessionUrl == null)
            endSessionUrl = generateProtocolPrefixUrl() + endSessionSuffix;
        return endSessionUrl;
    }

    private void generateIssuerUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(authUrl);
        if (stringBuilder.charAt(stringBuilder.length() - 1) == '/')
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(realmSeparator);
        stringBuilder.append(realm);
        issuerUrl = stringBuilder.toString();
    }

    private String generateProtocolPrefixUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getIssuerUrl());
        if (stringBuilder.charAt(stringBuilder.length() - 1) == '/')
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(protocolSeparator);
        return stringBuilder.toString();
    }
}
