package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote.external;

import lombok.Builder;

@Builder
record FinancialInstrumentSubscribeResponseDto(Long id,
                                               String name,
                                               String symbol) {
}

