package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
