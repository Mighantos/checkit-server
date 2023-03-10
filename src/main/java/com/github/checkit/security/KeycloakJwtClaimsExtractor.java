package com.github.checkit.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.oauth2.jwt.Jwt;

public final class KeycloakJwtClaimsExtractor {
    public static final String RESOURCE_ACCESS = "resource_access";
    public static final String ROLES = "roles";
    public static final String USERNAME = "preferred_username";
    public static final String EMAIL = "email";
    public static final String ROLE_PREFIX = "ROLE_";

    public static String extractEmail(Jwt jwt) {
        return (String) jwt.getClaims().get(EMAIL);
    }


    public static String extractUsername(Jwt jwt) {
        return (String) jwt.getClaims().get(USERNAME);
    }

    public static List<String> extractAuthorities(Jwt jwt, String clientId) {
        List<String> authorities = new ArrayList<>();
        Map<String, Object> resourceAccess = (Map) jwt.getClaims().get(KeycloakJwtClaimsExtractor.RESOURCE_ACCESS);
        if (Objects.nonNull(resourceAccess)) {
            Map<String, Object> checkItResourceAccess = (Map) resourceAccess.get(clientId);
            if (Objects.nonNull(checkItResourceAccess)) {
                List<String> roles = (List) checkItResourceAccess.get(KeycloakJwtClaimsExtractor.ROLES);
                if (Objects.nonNull(roles)) {
                    authorities.addAll(roles);
                }
            }
        }
        return authorities;
    }
}
