package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import lombok.Builder;

@Builder
record FinancialInstrumentResponseDto(Long id,
                                      String name,
                                      String symbol) {
}

