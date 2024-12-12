package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception;

public class ModelNotFoundException extends RuntimeException {
    public ModelNotFoundException(String message) {
        super(message);
    }
}
