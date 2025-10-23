package com.piotrgrochowiecki.manager.domain.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class DataLoaderParametersProvider {

    @Value("${dataLoader.activeThreshold.millisecond}")
    private int activeThresholdMilliseconds;

    @Value("${dataLoader.recommendedNumberOfFinancialInstrumentsPerDataLoader}")
    private int recommendedNumberOfFinancialInstrumentsPerDataLoader;

    @Value("${dataLoader.numberOfDataLoadersHandledByManagerInOneReassignmentCycle}")
    private int numberOfDataLoadersHandledByManagerInOneCycle;

}
