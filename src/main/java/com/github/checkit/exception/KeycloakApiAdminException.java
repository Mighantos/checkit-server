package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class KeycloakApiAdminException extends BaseException {

    public KeycloakApiAdminException() {
        super("Modifying API admin user is not allowed.");
    }

    public KeycloakApiAdminException(String message) {
        super(message);
    }
}
