package com.github.checkit.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class PublicationContextIsClosedException extends BaseException {

    public PublicationContextIsClosedException(String message, Object... args) {
        super(message, args);
    }

    public static PublicationContextIsClosedException create(URI changeUri) {
        return new PublicationContextIsClosedException("Publication context of change \"%s\" is closed. You can no "
            + "longer change reviews.", changeUri);
    }
}
