package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model;

import lombok.Builder;

import java.time.Instant;

@Builder
public record DataLoaderModel(Long id,
                              String uuid,
                              Instant lastConnected) {
}
