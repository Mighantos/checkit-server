package com.github.checkit.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message, Object... args) {
        super(message, args);
    }

    public static ForbiddenException createForbiddenToReviewPublicationContext(URI userUri, URI publicationContextUri) {
        return new ForbiddenException("User \"%s\" can't review publication context \"%s\".", userUri,
            publicationContextUri);
    }

    public static ForbiddenException createForbiddenToReviewChange(URI userUri, URI changeUri) {
        return new ForbiddenException("User \"%s\" can't review change \"%s\".", userUri, changeUri);
    }
}
