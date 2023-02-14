package com.github.checkit.security;


import com.github.checkit.config.properties.KeycloakConfigProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final KeycloakConfigProperties keycloakConfigProperties;

    public KeycloakJwtGrantedAuthoritiesConverter(KeycloakConfigProperties keycloakConfigProperties) {
        this.keycloakConfigProperties = keycloakConfigProperties;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : getAuthorities(jwt)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        return grantedAuthorities;
    }

    private List<String> getAuthorities(Jwt jwt) {
        List<String> authorities = new ArrayList<>();
        Map<String, Object> resourceAccess = (Map) jwt.getClaims().get(KeycloakJwtClaimsConstants.RESOURCE_ACCESS);
        if (Objects.nonNull(resourceAccess)) {
            Map<String, Object> checkItResourceAccess = (Map) resourceAccess.get(keycloakConfigProperties.getClientId());
            if (Objects.nonNull(checkItResourceAccess)) {
                List<String> roles = (List) checkItResourceAccess.get(KeycloakJwtClaimsConstants.ROLES);
                if (Objects.nonNull(roles)) {
                    authorities.addAll(roles);
                }
            }
        }
        return authorities;
    }
}
