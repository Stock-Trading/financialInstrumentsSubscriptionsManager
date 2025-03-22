package com.piotrgrochowiecki.manager.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class SubscriptionModel{

        private String dataLoaderUUID;
        private List<FinancialInstrumentModel> financialInstrumentModelCollection;

}