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
    private Boolean active;
    private Boolean readyForHandling;
    private DataLoaderLoadStatus loadStatus;
    private Collection<FinancialInstrumentModel> financialInstrumentModelCollection;

    public enum DataLoaderLoadStatus {
        TOO_HIGH,
        TOO_LOW,
        BALANCED
    }
}
