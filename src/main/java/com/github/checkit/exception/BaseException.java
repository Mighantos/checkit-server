package com.github.checkit.exception;

abstract class BaseException extends RuntimeException {

    protected BaseException() {
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Object... args) {
        super(String.format(message, args));
    }
}
