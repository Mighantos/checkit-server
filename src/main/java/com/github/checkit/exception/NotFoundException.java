package com.github.checkit.exception;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Object... args) {
        super(message, args);
    }

    public static NotFoundException create(String resourceName, Object identifier) {
        return new NotFoundException(resourceName + " identified by \"" + identifier + "\" not found.");
    }

    public static NotFoundException create(Class<?> resourceType, Object identifier) {
        Objects.requireNonNull(resourceType);
        return create(resourceType.getSimpleName(), identifier);
    }
}
