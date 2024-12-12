package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class FinancialInstrumentModel {

    private Long id;
    private String name;
    private String symbol;
    private DataLoaderModel dataLoader;

    public FinancialInstrumentModel(Long id, String name, String symbol, DataLoaderModel dataLoader) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.dataLoader = dataLoader;
    }
}
