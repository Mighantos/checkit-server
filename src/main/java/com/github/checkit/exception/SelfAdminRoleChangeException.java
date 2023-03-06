package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SelfAdminRoleChangeException extends BaseException {

    public SelfAdminRoleChangeException() {
        super("You can only modify Admin role of others, not your own.");
    }
}
