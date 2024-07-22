package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model;

import lombok.Builder;

import java.util.Collection;

@Builder
public record SubscriptionModel(String dataLoaderUUID,
                                Collection<FinancialInstrumentModel> financialInstrumentModelCollection) {

}