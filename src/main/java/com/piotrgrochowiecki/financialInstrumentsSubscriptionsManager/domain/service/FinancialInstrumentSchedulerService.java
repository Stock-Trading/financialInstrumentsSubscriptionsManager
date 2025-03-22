package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.usecase.AssignUnassignedFinancialInstrumentToDataLoadersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FinancialInstrumentSchedulerService {

    private final AssignUnassignedFinancialInstrumentToDataLoadersUseCase assignUnassignedFinancialInstrumentToDataLoadersUseCase;

    @Scheduled(fixedDelay = 10_000)
    public void assignUnassignedInstrumentsToActiveDataLoaders() {
        log.debug("Starting regular task of assigning unassigned Financial Instruments to active Data Loaders");
        assignUnassignedFinancialInstrumentToDataLoadersUseCase.assignUnassignedInstrumentsToActiveDataLoaders();
    }

}
