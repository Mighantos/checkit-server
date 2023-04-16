package com.github.checkit.exception;

import com.github.checkit.model.Change;
import com.github.checkit.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ChangeNotRejectedException extends BaseException {

    public ChangeNotRejectedException(String message, Object... args) {
        super(message, args);
    }

    public static ChangeNotRejectedException create(Change change, User user) {
        return new ChangeNotRejectedException("Change \"%s\" is not rejected by user \"%s\".", change.getUri(),
            user.getUri());
    }
}
