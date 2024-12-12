package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import lombok.Builder;

import java.util.List;

@Builder
record SubscriptionResponseDto(String dataLoaderUuid,
                               List<FinancialInstrumentResponseDto> financialInstrumentResponseDtoList) {
}
