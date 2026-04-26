package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class UnassignFinancialInstrumentFromAllInactiveDataLoaderUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;

    /**
     * Unassigns all financial instruments from inactive data loaders in a cleanup operation.
     *
     * <p>
     * <b>Algorithm Flow:</b>
     * </p>
     * <pre>
     * Cleanup from Inactive Data Loaders (Runs Every 1 Second - CRITICAL PRIORITY):
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 1: Retrieve All Inactive Data Loaders              │
     *     │   - Query database for DLs with active=false            │
     *     │   - Result: Collection of inactive Data Loaders         │
     *     │   - Extract their IDs into a list                       │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ├─ If collection is empty:
     *                        │    No inactive loaders found
     *                        │    Nothing to cleanup, return
     *                        │
     *                        └─ If collection has items: Process each
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 2: For Each Inactive Data Loader:                  │
     *     │   Iterate through all inactive loaders                  │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2a: Find All Assigned Financial Instruments        │
     *     │   - Query: Find all FIs with dataLoaderId = DL.id       │
     *     │   - Result: List of FinancialInstrumentModel            │
     *     │   - Count: Number of FIs currently assigned             │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ├─ If list is empty:
     *                        │    No FIs to unassign
     *                        │    Log: "0 assigned"
     *                        │    Skip to next loader
     *                        │
     *                        └─ If list has items: Unassign all
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2b: Collect Financial Instrument IDs               │
     *     │   - Extract ID from each FI object                      │
     *     │   - Build list: [FI_id_1, FI_id_2, FI_id_N]             │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2c: Batch Unassign All Financial Instruments       │
     *     │   - Execute batch SQL update:                           │
     *     │     SET dataLoaderId = null                             │
     *     │     WHERE id IN (collected FI IDs)                      │
     *     │   - Result: Count of affected rows                      │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2d: Update Data Loader Metadata                    │
     *     │   - Set: lastLoadStatusUpdate = Instant.now()           │
     *     │   - Purpose: Mark when cleanup occurred                 │
     *     │   - Persist: Save DL to database                        │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2e: Log Cleanup Results                            │
     *     │   - Unassigned X FIs from DL_Y                          │
     *     │   - Example: "Unassigned 3 FIs from DL_2"               │
     *     │   - Move to next inactive loader                        │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ COMPLETE: All Inactive Loaders Processed                │
     *     │   - All their FIs are now UNASSIGNED (null)             │
     *     │   - Ready for reassignment by Assignment Task (10s)     │
     *     │   - Failure recovery within 1 second of detection       │
     *     └─────────────────────────────────────────────────────────┘
     * </pre>
     *
     * <p>
     * <b>Execution Example - Data Loader Crash Scenario:</b>
     * </p>
     * <pre>
     * Before Cleanup:
     *   DL_2 (active=false) has:
     *     ├─ FI_100 (dataLoaderId=2)
     *     ├─ FI_101 (dataLoaderId=2)
     *     └─ FI_102 (dataLoaderId=2)
     *
     *   DL_3 (active=false) has:
     *     └─ FI_103 (dataLoaderId=3)
     *
     * Cleanup Task Execution:
     *   Step 1: Find inactive DLs → [DL_2, DL_3]
     *   Step 2a (DL_2): Find assigned FIs → [FI_100, FI_101, FI_102]
     *   Step 2b: Collect IDs → [100, 101, 102]
     *   Step 2c: Batch unassign → 3 rows affected
     *   Step 2d: Update DL_2 timestamp
     *   Step 2e: Log "Unassigned 3 FIs from DL_2"
     *
     *   Step 2a (DL_3): Find assigned FIs → [FI_103]
     *   Step 2b: Collect IDs → [103]
     *   Step 2c: Batch unassign → 1 row affected
     *   Step 2d: Update DL_3 timestamp
     *   Step 2e: Log "Unassigned 1 FI from DL_3"
     *
     * After Cleanup:
     *   DL_2 (active=false) has: (empty)
     *   DL_3 (active=false) has: (empty)
     *   Unassigned FIs ready for reassignment: [FI_100, FI_101, FI_102, FI_103]
     * </pre>
     *
     * <p>
     * <b>Key Responsibilities:</b>
     * </p>
     * <ul>
     *   <li>Query database for all data loaders with active flag = false</li>
     *   <li>For each inactive loader, retrieve all assigned financial instruments</li>
     *   <li>Detach all financial instruments from the inactive loader</li>
     *   <li>Update loader's last status update timestamp</li>
     *   <li>Persist changes to database</li>
     *   <li>Log all operations for auditing</li>
     * </ul>
     *
     * <p>
     * <b>Cleanup Rationale:</b>
     * </p>
     * <p>
     * This is a high-priority cleanup task that runs every 1 second (CRITICAL PRIORITY). It ensures that:
     * </p>
     * <ul>
     *   <li><b>Immediate Failure Detection:</b> Runs frequently to catch inactive loaders ASAP</li>
     *   <li><b>Rapid Instrument Release:</b> FIs freed within seconds of loader failure</li>
     *   <li><b>No Stale Assignments:</b> Prevents data inconsistency from failed loaders</li>
     *   <li><b>Fast Recovery:</b> Assignment task picks up freed FIs within next 10 seconds</li>
     *   <li><b>Atomic Operations:</b> Batch unassign ensures data consistency</li>
     * </ul>
     *
     * <p>
     * <b>Execution Context:</b>
     * </p>
     * <ul>
     *   <li>Executed by {@link com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentBalancingSchedulerService}</li>
     *   <li>Transactional - ensures all unassignments are atomic</li>
     *   <li>Depends on: Active state determination (STEP 2 marks DLs as inactive)</li>
     *   <li>Precedes: Assignment task (awaits freed FIs for reassignment)</li>
     * </ul>
     */
    @Transactional
    public void unassignFinancialInstrumentFromInactiveDataLoader() {
        log.info("Retrieving IDs of Data Loaders with Active flag set to false");
        Collection<DataLoaderModel> collection = dataLoaderRepository.findInactiveDataLoaders();
        List<Long> idsOfDataLoadersWithFalseActiveFlag = collection
                .stream()
                .map(DataLoaderModel::getId)
                .toList();
        log.debug("Ids of Data Loaders with Active flag set to false: {}",
                Arrays.toString(idsOfDataLoadersWithFalseActiveFlag.toArray()));

        collection.forEach(model -> {
            unassignAllFinancialInstrumentsFromDataLoader(model);
            model.setLastLoadStatusUpdate(Instant.now());
            dataLoaderRepository.save(model);
        });
    }

    private void unassignAllFinancialInstrumentsFromDataLoader(DataLoaderModel model) {
        Long idOfDataLoader = model.getId();
        log.debug("Unassigning all Financial Instruments from Data Loader with id {}", idOfDataLoader);
        List<FinancialInstrumentModel> financialInstrumentModels =
                financialInstrumentRepository.findByDataLoaderId(idOfDataLoader);
        log.debug("Number of Financial Instruments assigned to Data Loader id={} is {}",
                idOfDataLoader, financialInstrumentModels.size());
        if (financialInstrumentModels.isEmpty()) {
            return;
        }
        List<Long> financialInstrumentModelsIdsToNullifyDataLoaders = financialInstrumentModels.stream()
                .map(FinancialInstrumentModel::getId)
                .toList();
        log.debug("Unassigning Financial Instruments with ids={} from Data Loader id={}",
                financialInstrumentModelsIdsToNullifyDataLoaders.toArray(),
                idOfDataLoader);
        int numberOfAffectedRows =
                financialInstrumentRepository.detachDataLoaderBasedOnIds(financialInstrumentModelsIdsToNullifyDataLoaders);
        log.debug("Unassigned Data Loader (id={}) from {} Financial Instruments",
                idOfDataLoader, numberOfAffectedRows);
    }

}
