package com.github.checkit.dto;

import com.github.checkit.model.User;
import com.github.checkit.security.KeycloakJwtClaimsExtractor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@Getter
public class CurrentUserDto extends UserDto {
    private final String username;
    private final String email;
    private final List<String> roles;

    public CurrentUserDto(User user, Authentication auth) {
        super(user);
        this.username = extractUsername(auth);
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
