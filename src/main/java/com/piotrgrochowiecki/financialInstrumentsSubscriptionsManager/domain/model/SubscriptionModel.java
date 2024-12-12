package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class SubscriptionModel{

        private String dataLoaderUUID;
        private List<FinancialInstrumentModel> financialInstrumentModelCollection;

}