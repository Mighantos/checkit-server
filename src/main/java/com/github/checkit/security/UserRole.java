package com.github.checkit.security;

import java.util.ArrayList;
import java.util.List;

public final class UserRole {
    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";

    public static List<String> getRequiredRoles() {
        return new ArrayList<>(List.of(new String[] {USER, ADMIN}));
    }
}
