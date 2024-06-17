package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model;

import lombok.Builder;

@Builder
public record FinancialInstrumentModel(Long id,
                                       String name,
                                       String symbol) {
}
