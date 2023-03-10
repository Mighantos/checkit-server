package com.github.checkit.security;


import com.github.checkit.config.properties.KeycloakConfigProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final KeycloakConfigProperties keycloakConfigProperties;

    public KeycloakJwtGrantedAuthoritiesConverter(KeycloakConfigProperties keycloakConfigProperties) {
        this.keycloakConfigProperties = keycloakConfigProperties;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        List<String> authorities =
            KeycloakJwtClaimsExtractor.extractAuthorities(jwt, keycloakConfigProperties.getClientId());
        for (String authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        return grantedAuthorities;
    }
}
