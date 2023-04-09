package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UnexpectedRdfObjectException extends BaseException {
    public UnexpectedRdfObjectException() {
        super("Unexpected RDF Object found in changes.");
    }
}
