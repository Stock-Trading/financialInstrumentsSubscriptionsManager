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
    //    private final BalanceDataLoadersUseCase balanceDataLoadersUseCase;
    private final CheckDataLoaderLoadStatusUseCase checkDataLoaderLoadStatusUseCase;
    private final CheckForReadinessOfDataLoaderForAssignmentOfFinancialInstrumentsUseCase checkForReadinessOfDataLoaderForAssignmentOfFinancialInstrumentsUseCase;
    private final AssignUnassignedFinancialInstrumentToDataLoadersUseCase assignUnassignedFinancialInstrumentToDataLoadersUseCase;
    private final UnassignFinancialInstrumentFromDataLoaderUseCase unassignFinancialInstrumentFromDataLoaderUseCase;

    @Scheduled(fixedDelay = 3_000)
    void checkActiveState() {
        log.debug("Running regular Data Loaders active state check");
        checkActiveStateOfDataLoaderUseCase.checkActiveState();
    }

        @Scheduled(fixedDelay = 4_000)
    void checkIfReadyForAssignmentOfFinancialInstruments() {
        log.debug("Running regular task of checking of Data Loaders readiness assignment of Financial Instruments");
        checkForReadinessOfDataLoaderForAssignmentOfFinancialInstrumentsUseCase.checkIfDataLoadersAreReadyForAssignmentOfFinancialInstruments();
    }

    //    @Scheduled(fixedDelay = 5_000)
    void checkLoadStatus() {
        log.debug("Running regular Data Loaders load status check");
        checkDataLoaderLoadStatusUseCase.checkLoadStatus();
    }

    //    @Scheduled(fixedDelay = 10_000)
    public void assignUnassignedInstrumentsToActiveDataLoaders() {
        log.debug("Running regular task of assigning unassigned Financial Instruments to active Data Loaders");
        assignUnassignedFinancialInstrumentToDataLoadersUseCase.assignUnassignedInstrumentsToActiveDataLoaders();
    }

    //    @Scheduled(fixedDelay = 12_000) //TODO rozważyć zmniejszenie częstotliwości
    public void unassignFinancialInstrumentsFromDataLoadersWithTooHighLoadStatus() {
        log.debug("Running regular task of unassigning Financial Instruments from Data Loaders with too high load status");
        unassignFinancialInstrumentFromDataLoaderUseCase.unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus();
    }
//    @Scheduled(fixedDelay = 17_500)
//    void balanceDataLoaders() {
//        log.debug("Running regular task of re-balancing Financial Instruments assigned to Data Loaders");
//        balanceDataLoadersUseCase.balanceDataLoaders();
//    }
}
