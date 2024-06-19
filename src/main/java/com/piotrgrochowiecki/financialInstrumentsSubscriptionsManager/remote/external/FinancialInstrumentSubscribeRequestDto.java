package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote.external;

import lombok.Builder;

@Builder
record FinancialInstrumentSubscribeRequestDto(String name,
                                              String symbol) {
}
