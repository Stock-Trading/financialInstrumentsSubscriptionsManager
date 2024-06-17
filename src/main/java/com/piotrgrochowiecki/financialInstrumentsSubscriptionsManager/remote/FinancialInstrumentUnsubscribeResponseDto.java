package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import lombok.Builder;

@Builder
record FinancialInstrumentUnsubscribeResponseDto(Long id,
                                                 String name,
                                                 String symbol) {
}
