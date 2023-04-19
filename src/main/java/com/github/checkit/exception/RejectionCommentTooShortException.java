package com.github.checkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RejectionCommentTooShortException extends BaseException {
    public RejectionCommentTooShortException(String message, Object... args) {
        super(message, args);
    }

    public static RejectionCommentTooShortException create(int len) {
        return new RejectionCommentTooShortException("Rejection comment content too short. Final comment must be at "
            + "least %d characters long.", len);
    }
}
