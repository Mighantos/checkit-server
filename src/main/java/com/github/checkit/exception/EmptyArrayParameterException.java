package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyArrayParameterException extends BaseException {

    public EmptyArrayParameterException(String message, Object... args) {
        super(message, args);
    }
}
