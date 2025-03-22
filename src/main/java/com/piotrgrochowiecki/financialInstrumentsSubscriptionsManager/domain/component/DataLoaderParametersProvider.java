package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class DataLoaderParametersProvider {

    @Value("${dataLoader.lastCheckInHealthThreshold.millisecond}")
    private int lastCheckedInHealthThresholdMilliseconds;

    @Value("${dataLoader.recommendedNumberOfFinancialInstrumentsPerDataLoader}")
    private int recommendedNumberOfFinancialInstrumentsPerDataLoader;

    @Value("${dataLoader.timeThresholdOfHandlingReadiness.millisecond}")
    private int timeThresholdOfHandlingReadinessMilliseconds;

}
