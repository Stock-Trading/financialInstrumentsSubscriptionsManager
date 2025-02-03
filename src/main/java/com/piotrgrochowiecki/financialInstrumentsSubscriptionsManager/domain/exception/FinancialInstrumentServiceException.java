package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception;

public class FinancialInstrumentServiceException extends RuntimeException {
    public FinancialInstrumentServiceException(String message) {
        super(message);
    }
}
