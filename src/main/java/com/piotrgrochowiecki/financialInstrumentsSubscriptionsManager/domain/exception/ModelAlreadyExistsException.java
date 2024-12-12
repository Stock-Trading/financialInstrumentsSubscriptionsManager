package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception;

public class ModelAlreadyExistsException extends RuntimeException{

    public ModelAlreadyExistsException(String message) {
        super(message);
    }
}
