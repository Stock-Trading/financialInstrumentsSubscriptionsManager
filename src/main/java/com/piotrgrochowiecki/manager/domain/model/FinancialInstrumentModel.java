package com.piotrgrochowiecki.manager.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class FinancialInstrumentModel {

    private Long id;
    private String name;
    private String symbol;
    @Nullable
    private Long dataLoaderId;

    public FinancialInstrumentModel(Long id, String name, String symbol, @Nullable Long dataLoaderId) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.dataLoaderId = dataLoaderId;
    }
}
