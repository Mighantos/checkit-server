package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class LoadingCustomEntityException extends BaseException {
    public LoadingCustomEntityException(String message, Object... args) {
        super(message, args);
    }

    public static LoadingCustomEntityException create(Class<?> entityType) {
        return new LoadingCustomEntityException("Could not load custom entity \"%s\" from DB.",
            entityType.getSimpleName());
    }
}
