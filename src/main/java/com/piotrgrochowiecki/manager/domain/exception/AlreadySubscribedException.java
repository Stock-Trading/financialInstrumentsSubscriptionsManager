package com.piotrgrochowiecki.manager.domain.exception;

public class AlreadySubscribedException extends RuntimeException {

    public AlreadySubscribedException(String message) {
        super(message);
    }
}
