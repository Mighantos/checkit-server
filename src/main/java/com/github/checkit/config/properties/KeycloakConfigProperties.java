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

    private final String apiClientId = "admin-cli";

    @Nonnull
    private String realm;

    @Nonnull
    private String clientId;

    @Nonnull
    private String url;

    @Nonnull
    private String realmKey;

    @Nonnull
    private String secret;

    @Nonnull
    private ApiAdminCredentials apiAdmin;

    private String issuerUrl;

    private String authorizationUrl;

    private String tokenUrl;

    private String userInfoUrl;

    private String jwksUrl;

    private String endSessionUrl;

    /**
     * Generates and returns issuer URL if not specified.
     *
     * @return issuer URL
     */
    public String getIssuerUrl() {
        if (issuerUrl == null) {
            generateIssuerUrl();
        }
        return issuerUrl;
    }

    /**
     * Generates and returns JSON Web Key Sets URL if not specified.
     *
     * @return JWKS URL
     */
    public String getJwksUrl() {
        if (jwksUrl == null) {
            jwksUrl = generateProtocolPrefixUrl() + jwksSuffix;
        }
        return jwksUrl;
    }

    /**
     * Generates and returns authorization URL if not specified.
     *
     * @return authorization URL
     */
    public String getAuthorizationUrl() {
        if (authorizationUrl == null) {
            authorizationUrl = generateProtocolPrefixUrl() + authorizationSuffix;
        }
        return authorizationUrl;
    }

    /**
     * Generates and returns token issuer URL if not specified.
     *
     * @return token issuer URL
     */
    public String getTokenUrl() {
        if (tokenUrl == null) {
            tokenUrl = generateProtocolPrefixUrl() + tokenSuffix;
        }
        return tokenUrl;
    }

    /**
     * Generates and returns user info URL if not specified.
     *
     * @return user info URL
     */
    public String getUserInfoUrl() {
        if (userInfoUrl == null) {
            userInfoUrl = generateProtocolPrefixUrl() + userInfoSuffix;
        }
        return userInfoUrl;
    }

    /**
     * Generates and returns URL to end session if not specified.
     *
     * @return end session URL
     */
    public String getEndSessionUrl() {
        if (endSessionUrl == null) {
            endSessionUrl = generateProtocolPrefixUrl() + endSessionSuffix;
        }
        return endSessionUrl;
    }

    private void generateIssuerUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url);
        if (stringBuilder.charAt(stringBuilder.length() - 1) == '/') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append(realmSeparator);
        stringBuilder.append(realm);
        issuerUrl = stringBuilder.toString();
    }

    private String generateProtocolPrefixUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getIssuerUrl());
        if (stringBuilder.charAt(stringBuilder.length() - 1) == '/') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append(protocolSeparator);
        return stringBuilder.toString();
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties("apiadmin")
    public static class ApiAdminCredentials {

        @Nonnull
        private String username;

        @Nonnull
        private String password;
    }
}
