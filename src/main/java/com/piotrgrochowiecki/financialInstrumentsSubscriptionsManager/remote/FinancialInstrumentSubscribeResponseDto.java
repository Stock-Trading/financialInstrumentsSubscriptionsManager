package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import lombok.Builder;

@Builder
record FinancialInstrumentSubscribeResponseDto(Long id,
                                               String name,
                                               String symbol) {
}

