package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.OK)
public class NoChangeException extends BaseException {

    public NoChangeException() {
        super("There were no changes found to create publication context.");
    }
}
