package com.github.checkit.dto;

import com.github.checkit.model.User;
import com.github.checkit.security.KeycloakJwtClaimsExtractor;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CurrentUserDto {
    private final String id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final List<String> roles;

    public CurrentUserDto(User user, Authentication auth) {
        this.id = user.getId();
        this.username = extractUsername(auth);
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = extractEmail(auth);
        this.roles = new ArrayList<>();
        setRolesFromAuthorities(auth.getAuthorities());
    }

    private void setRolesFromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority ga : authorities) {
            String authority = ga.getAuthority();
            if (authority.startsWith(KeycloakJwtClaimsExtractor.ROLE_PREFIX)) {
                roles.add(authority);
            }
        }
    }

    private String extractEmail(Authentication auth) {
        Jwt jwt = (Jwt) auth.getCredentials();
        return KeycloakJwtClaimsExtractor.extractEmail(jwt);
    }


    private String extractUsername(Authentication auth) {
        Jwt jwt = (Jwt) auth.getCredentials();
        return KeycloakJwtClaimsExtractor.extractUsername(jwt);
    }
}
