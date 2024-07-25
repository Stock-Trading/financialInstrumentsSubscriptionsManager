package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model;

import lombok.Builder;

import java.time.Instant;
import java.util.Collection;

@Builder
public record DataLoaderModel(Long id,
                              String uuid,
                              Instant lastConnected,
                              Collection<FinancialInstrumentModel> financialInstrumentModelCollection) {
}
