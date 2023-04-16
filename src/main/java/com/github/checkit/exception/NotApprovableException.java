package com.github.checkit.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NotApprovableException extends BaseException {

    public NotApprovableException(String message, Object... args) {
        super(message, args);
    }

    public static NotApprovableException create(URI publicationContextUri) {
        return new NotApprovableException("Some vocabularies of publication context \"%s\" were not approved.",
            publicationContextUri);
    }
}
