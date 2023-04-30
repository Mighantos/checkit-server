package com.github.checkit.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NotificationAlreadyReadException extends BaseException {
    public NotificationAlreadyReadException(String message, Object... args) {
        super(message, args);
    }

    public static NotificationAlreadyReadException create(URI notificationUri) {
        return new NotificationAlreadyReadException("Notification identified by \"%s\" is already read.",
            notificationUri);
    }
}
