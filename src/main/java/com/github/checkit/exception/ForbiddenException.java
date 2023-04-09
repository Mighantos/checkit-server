package com.github.checkit.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message, Object... args) {
        super(message, args);
    }

    public static ForbiddenException createForbiddenToReview(URI userUri, URI changeUri) {
        return new ForbiddenException("User \"%s\" can't review change \"%s\".", userUri, changeUri);
    }
}
