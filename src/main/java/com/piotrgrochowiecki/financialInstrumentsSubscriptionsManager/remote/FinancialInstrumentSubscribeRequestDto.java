package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import lombok.Builder;

@Builder
record FinancialInstrumentSubscribeRequestDto(String name,
                                              String symbol) {
}
