package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FinalCommentTooShortException extends BaseException {
    public FinalCommentTooShortException(String message, Object... args) {
        super(message, args);
    }

    public static FinalCommentTooShortException create(int len) {
        return new FinalCommentTooShortException("Final comment content too short. Final comment must be at least %d "
            + "characters long.", len);
    }
}
