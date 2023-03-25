package com.github.checkit.exception;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super("HTTP 403 Forbidden");
    }
}
