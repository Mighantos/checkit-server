package com.github.checkit.exception;

import com.github.checkit.model.ChangeType;
import org.apache.jena.rdf.model.Statement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class WrongChangeTypeException extends BaseException {
    public WrongChangeTypeException(String message, Object... args) {
        super(message, args);
    }

    public static WrongChangeTypeException create(Statement statement) {
        return new WrongChangeTypeException("Statement \"%s\" was marked as %s, but modified version of this "
            + "statement was not found.", statement, ChangeType.MODIFIED);
    }
}
