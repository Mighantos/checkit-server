package com.github.checkit.dto;

import com.github.checkit.model.User;
import com.github.checkit.security.KeycloakJwtClaimsConstants;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class UserDto {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final List<String> roles;

    public UserDto(User user, Collection<? extends GrantedAuthority> authorities) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = new ArrayList<>();
        setRolesFromAuthorities(authorities);
    }

    private void setRolesFromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority ga : authorities) {
            String authority = ga.getAuthority();
            if (authority.startsWith(KeycloakJwtClaimsConstants.ROLE_PREFIX)) {
                roles.add(authority);
            }
        }
    }
}
