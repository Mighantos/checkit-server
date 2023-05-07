package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SgovPublishException extends BaseException {

    public SgovPublishException(String message) {
        super(message);
    }
}
