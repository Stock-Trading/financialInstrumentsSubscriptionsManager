package com.piotrgrochowiecki.manager.domain.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class FinancialInstrumentParametersProvider {

    @Value("${financialInstrument.recommendedNumberOfFinancialInstrumentsUnassignedToAnyDataLoaderToBeHandledBySingleInstance}")
    private int recommendedNumberOfFinancialInstrumentsUnassignedToAnyDataLoader;

}
