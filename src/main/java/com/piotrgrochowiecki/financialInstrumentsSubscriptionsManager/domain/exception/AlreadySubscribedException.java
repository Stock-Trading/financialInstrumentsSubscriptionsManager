package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception;

public class AlreadySubscribedException extends RuntimeException {

    public AlreadySubscribedException(String message) {
        super(message);
    }
}
