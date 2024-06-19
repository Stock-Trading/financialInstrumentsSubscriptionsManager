package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote.external;

import lombok.Builder;

@Builder
record FinancialInstrumentUnsubscribeResponseDto(Long id,
                                                 String name,
                                                 String symbol) {
}
