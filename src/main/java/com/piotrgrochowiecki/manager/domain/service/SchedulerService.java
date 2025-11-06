package com.piotrgrochowiecki.manager.domain.service;

import com.piotrgrochowiecki.manager.domain.usecase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final CheckActiveStateOfDataLoaderUseCase checkActiveStateOfDataLoaderUseCase;
    private final CheckDataLoaderLoadStatusUseCase checkDataLoaderLoadStatusUseCase;
    private final AssignUnassignedFinancialInstrumentToDataLoadersUseCase assignUnassignedFinancialInstrumentToDataLoadersUseCase;
    private final UnassignFinancialInstrumentFromDataLoaderUseCase unassignFinancialInstrumentFromDataLoaderUseCase;

    @Scheduled(fixedDelay = 3_000)
    void checkActiveState() {
        log.debug("Running regular Data Loaders active state check");
        checkActiveStateOfDataLoaderUseCase.checkActiveState();
    }

    @Scheduled(fixedDelay = 5_000)
    void checkLoadStatus() {
        log.debug("Running regular Data Loaders load status check");
        checkDataLoaderLoadStatusUseCase.checkLoadStatus();
    }

    @Scheduled(fixedDelay = 10_000)
    public void assignUnassignedInstrumentsToActiveDataLoaders() {
        log.debug("Running regular task of assigning unassigned Financial Instruments to active Data Loaders");
        assignUnassignedFinancialInstrumentToDataLoadersUseCase.assignUnassignedInstrumentsToActiveDataLoaders();
    }

    @Scheduled(fixedDelay = 12_000)
    public void unassignFinancialInstrumentsFromDataLoadersWithTooHighLoadStatus() {
        log.debug("Running regular task of unassigning Financial Instruments from Data Loaders with too high load status");
        unassignFinancialInstrumentFromDataLoaderUseCase.unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus();
    }

}
