package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class DataLoaderSchedulerService {

    private final DataLoaderService dataLoaderService;

    @Scheduled(fixedDelay = 3000)
    void checkActiveState() {
        log.debug("Running regular Data Loaders active state check");
        dataLoaderService.checkActiveState();
    }

    @Scheduled(fixedDelay = 4000)
    void checkReadyForHandlingStatus() {
        log.debug("Running regular Data Loaders readiness for handling check");
        dataLoaderService.checkReadyForHandlingStatus();
    }

    @Scheduled(fixedDelay = 5000)
    void checkLoadStatus() {
        log.debug("Running regular Data Loaders load status check");
        dataLoaderService.checkLoadStatus();
    }

    @Scheduled(fixedDelay = 17_500)
    void balanceDataLoaders() {
        log.debug("Running regular task of re-balancing Financial Instruments assigned to Data Loaders");
        dataLoaderService.balanceDataLoaders();
    }
}
