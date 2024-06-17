package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import lombok.Builder;

@Builder
record FinancialInstrumentUnsubscribeRequestDto(Long id,
                                                String name,
                                                String symbol) {
}