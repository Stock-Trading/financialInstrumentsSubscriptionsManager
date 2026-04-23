package com.piotrgrochowiecki.manager.domain.service;

import com.piotrgrochowiecki.manager.domain.usecase.loadbalance.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Orchestrates the load balancing algorithm for distributing financial instruments across active data loaders.
 * </p>
 *
 * <p>
 * This service implements a continuous load balancing mechanism by regularly invoking a series of coordinated
 * methods at defined intervals. The algorithm maintains optimal distribution of financial instruments by:
 * </p>
 * <ul>
 *   <li>Monitoring the health and active status of data loaders</li>
 *   <li>Tracking load metrics and capacity of each data loader</li>
 *   <li>Distributing unassigned financial instruments to active data loaders with available capacity</li>
 *   <li>Rebalancing load by unassigning instruments from overloaded or inactive data loaders</li>
 * </ul>
 *
 * <p>
 * <b>Execution Schedule and Order:</b>
 * </p>
 * <p>
 * The algorithm executes the following methods at defined intervals (all running continuously and independently):
 * </p>
 * <ol>
 *   <li><b>Every 1 second:</b> Unassign financial instruments from inactive data loaders (highest priority cleanup)</li>
 *   <li><b>Every 3 seconds:</b> Check and update active state of all data loaders</li>
 *   <li><b>Every 5 seconds:</b> Check and update load status metrics of all data loaders</li>
 *   <li><b>Every 10 seconds:</b> Assign unassigned financial instruments to active data loaders with available capacity</li>
 *   <li><b>Every 12 seconds:</b> Unassign financial instruments from data loaders exceeding load thresholds</li>
 * </ol>
 *
 * <p>
 * <b>Key Responsibilities:</b>
 * </p>
 * <ul>
 *   <li>Ensure high availability through continuous health checks and rebalancing</li>
 *   <li>Maintain balanced load distribution across all active data loaders</li>
 *   <li>React to changes in data loader status and capacity</li>
 *   <li>Automatically recover from data loader failures by reassigning instruments</li>
 * </ul>
 *
 * <p>
 * See {@code LOAD_BALANCING_ALGORITHM.md} for a detailed visual representation of the algorithm flow.
 * </p>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class FinancialInstrumentBalancingSchedulerService {

    private final CheckActiveStateOfDataLoaderUseCase checkActiveStateOfDataLoaderUseCase;
    private final CheckDataLoaderLoadStatusUseCase checkDataLoaderLoadStatusUseCase;
    private final AssignUnassignedFinancialInstrumentToDataLoadersUseCase assignUnassignedFinancialInstrumentToDataLoadersUseCase;
    private final UnassignFinancialInstrumentFromDataLoaderWithTooHighLoadStatusUseCase unassignFinancialInstrumentFromDataLoaderUseCase;
    private final UnassignFinancialInstrumentFromAllInactiveDataLoaderUseCase unassignFinancialInstrumentFromAllInactiveDataLoader;

    @Scheduled(fixedDelay = 1_000)
    void unassignFinancialInstrumentsFromInactiveDataLoaders() {
        log.debug("Running regular task of unassigning Financial Instruments from inactive Data Loaders");
        unassignFinancialInstrumentFromAllInactiveDataLoader.unassignFinancialInstrumentFromInactiveDataLoader();
    }

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
