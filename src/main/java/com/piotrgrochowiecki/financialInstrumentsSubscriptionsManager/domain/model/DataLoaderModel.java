package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Collection;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class DataLoaderModel {

    private Long id;
    private String uuid;
    private Instant lastConnectedOn;
    private Instant lastHandledOn;
    private Collection<FinancialInstrumentModel> financialInstrumentModelCollection;

    public DataLoaderModel(Long id, String uuid, Instant lastConnectedOn, Instant lastHandledOn, Collection<FinancialInstrumentModel> financialInstrumentModelCollection) {
        this.id = id;
        this.uuid = uuid;
        this.lastConnectedOn = lastConnectedOn;
        this.lastHandledOn = lastHandledOn;
        this.financialInstrumentModelCollection = financialInstrumentModelCollection;
    }
}
