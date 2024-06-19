package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote.external;

import lombok.Builder;

@Builder
record FinancialInstrumentResponseDto(Long id,
                                      String name,
                                      String symbol) {
}

