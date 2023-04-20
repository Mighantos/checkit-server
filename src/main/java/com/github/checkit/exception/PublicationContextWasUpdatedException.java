package com.github.checkit.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class PublicationContextWasUpdatedException extends BaseException {

    public PublicationContextWasUpdatedException(String message, Object... args) {
        super(message, args);
    }

    public static PublicationContextWasUpdatedException create(URI changeUri) {
        return new PublicationContextWasUpdatedException("Publication context of change \"%s\" was updated, load new "
            + "changes.", changeUri);
    }
}
