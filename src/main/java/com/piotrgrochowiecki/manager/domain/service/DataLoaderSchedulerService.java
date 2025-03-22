package com.piotrgrochowiecki.manager.domain.service;

import com.piotrgrochowiecki.manager.domain.usecase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class DataLoaderSchedulerService {

    private final CheckActiveStateOfDataLoaderUseCase checkActiveStateOfDataLoaderUseCase;
    private final BalanceDataLoadersUseCase balanceDataLoadersUseCase;
    private final CheckDataLoaderLoadStatusUseCase checkDataLoaderLoadStatusUseCase;
    private final CheckReadyForHandlingStatusOfDataLoaderUseCase checkReadyForHandlingStatusOfDataLoaderUseCase;

    @Scheduled(fixedDelay = 3000)
    void checkActiveState() {
        log.debug("Running regular Data Loaders active state check");
        checkActiveStateOfDataLoaderUseCase.checkActiveState();
    }

    @Scheduled(fixedDelay = 4000)
    void checkReadyForHandlingStatus() {
        log.debug("Running regular Data Loaders readiness for handling check");
        checkReadyForHandlingStatusOfDataLoaderUseCase.checkReadyForHandlingStatus();
    }

    @Scheduled(fixedDelay = 5000)
    void checkLoadStatus() {
        log.debug("Running regular Data Loaders load status check");
        checkDataLoaderLoadStatusUseCase.checkLoadStatus();
    }

    @Scheduled(fixedDelay = 17_500)
    void balanceDataLoaders() {
        log.debug("Running regular task of re-balancing Financial Instruments assigned to Data Loaders");
        balanceDataLoadersUseCase.balanceDataLoaders();
    }
}
