package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote.external;

import lombok.Builder;

@Builder
record FinancialInstrumentUnsubscribeRequestDto(Long id,
                                                String name,
                                                String symbol) {
}