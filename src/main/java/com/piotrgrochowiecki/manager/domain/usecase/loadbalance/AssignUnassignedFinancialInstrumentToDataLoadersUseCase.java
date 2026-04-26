package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.component.FinancialInstrumentParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class AssignUnassignedFinancialInstrumentToDataLoadersUseCase {

    private final FinancialInstrumentService financialInstrumentService;
    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;
    private final FinancialInstrumentParametersProvider financialInstrumentParametersProvider;

    /**
     * Assigns unassigned financial instruments to active data loaders with available capacity.
     *
     * <p>
     * <b>Algorithm Flow:</b>
     * </p>
     * <pre>
     * Assignment Process:
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 1: Check for Unassigned FIs                        │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ├─ If none exist:
     *                        │    Log "All assigned" and return
     *                        │
     *                        └─ If some exist: Continue
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 2: Batch Retrieve Unassigned FIs                   │
     *     │   - Order by: CREATED_ON_ASC (oldest first)             │
     *     │   - Limit: Configurable batch size                      │
     *     │   - Result: List of unassigned Financial Instruments    │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 3: Find Available Data Loaders                     │
     *     │   - Status: TOO_LOW or NULL (have capacity)             │
     *     │   - Active: true (currently responsive)                 │
     *     │   - Order by: LAST_CONNECTED_ON_ASC (oldest first)      │
     *     │   - Limit: Configurable batch size                      │
     *     │   - Result: List of available Data Loaders              │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ├─ If DLs empty: Log and return
     *                        │
     *                        └─ If DLs available: Continue
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 4: Round-Robin Assignment                          │
     *     │   For each FI at index i:                               │
     *     │     Assign FI to DL[i % DL_count]                       │
     *     │     Update FI.dataLoaderId = DL.id in database          │
     *     │     Log the assignment                                  │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ COMPLETE: All FIs Assigned                              │
     *     │   - Unassigned FIs now have a data loader               │
     *     │   - Ready for monitoring and data collection            │
     *     │   - Next cycle will assess their load status            │
     *     └─────────────────────────────────────────────────────────┘
     * </pre>
     *
     * <p>
     * <b>Assignment Strategy - Round-Robin Distribution:</b>
     * </p>
     * <pre>
     * Example with 5 FIs and 3 available DLs:
     *
     *     Unassigned FIs: [FI_10, FI_11, FI_12, FI_13, FI_14]
     *     Available DLs:  [DL_3, DL_5, DL_7]
     *
     *     Assignment Calculation:
     *       FI_10 (index 0) → DL[0 % 3] = DL_3
     *       FI_11 (index 1) → DL[1 % 3] = DL_5
     *       FI_12 (index 2) → DL[2 % 3] = DL_7
     *       FI_13 (index 3) → DL[3 % 3] = DL_3
     *       FI_14 (index 4) → DL[4 % 3] = DL_5
     *
     *     Final Distribution:
     *       DL_3: FI_10, FI_13  (2 instruments)
     *       DL_5: FI_11, FI_14  (2 instruments)
     *       DL_7: FI_12         (1 instrument)
     *
     *     Result: Even distribution across all available loaders
     * </pre>
     *
     * <p>
     * <b>Key Responsibilities:</b>
     * </p>
     * <ul>
     *   <li>Check if any unassigned financial instruments exist</li>
     *   <li>Batch-retrieve unassigned financial instruments with configurable batch size</li>
     *   <li>Batch-retrieve active data loaders with capacity (TOO_LOW or NULL status)</li>
     *   <li>Perform round-robin distribution of FIs to DLs</li>
     *   <li>Update and persist each assigned financial instrument</li>
     *   <li>Log all assignments for monitoring</li>
     * </ul>
     *
     * <p>
     * <b>Execution Context:</b>
     * </p>
     * <ul>
     *   <li>Executed by {@link com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentBalancingSchedulerService}</li>
     *   <li>Transactional - ensures assignments are atomic</li>
     * </ul>
     */

    @Transactional
    public void assignUnassignedInstrumentsToActiveDataLoaders() {
        if (!financialInstrumentRepository.existsWithNoDataLoaderAssigned()) {
            log.debug("All financial instruments are assigned to data loaders.");
            return;
        }
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(
                financialInstrumentRepository.findUnassignedToAnyDataLoader(
                        FinancialInstrumentRepository.OrderBy.CREATED_ON_ASC,
                        financialInstrumentParametersProvider.getRecommendedNumberOfFinancialInstrumentsUnassignedToAnyDataLoader()));
        List<DataLoaderModel> dataLoadersReadyForAssignmentOfFinancialInstruments = new LinkedList<>(
                dataLoaderRepository.findActiveDataLoadersWithTooLowOrNullLoadStatus(
                        DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                        dataLoaderParametersProvider.getNumberOfDataLoadersHandledByManagerInOneCycle()));
        log.info("List of Data Loaders ready for assignment of Financial Instruments {}", dataLoadersReadyForAssignmentOfFinancialInstruments.toString());
        assignFIsToDLs(unassignedFIs, dataLoadersReadyForAssignmentOfFinancialInstruments);
    }

    private void assignFIsToDLs(List<FinancialInstrumentModel> financialInstrumentModelList,
                                List<DataLoaderModel> dataLoaderModelList) {
        if (financialInstrumentModelList.isEmpty() || dataLoaderModelList.isEmpty()) {
            log.debug("Financial Instrument list or Data Loaders list is empty. Cannot assign FIs to DLs.");
            return;
        }

        for (int i = 0; i < financialInstrumentModelList.size(); i++) {
            FinancialInstrumentModel financialInstrument = financialInstrumentModelList.get(i);
            DataLoaderModel dataLoader = dataLoaderModelList.get(i % dataLoaderModelList.size());
            financialInstrument.setDataLoaderId(dataLoader.getId());
            log.debug("Assigned Data Loader with id {} to Financial Instrument {}, id={}",
                    dataLoader.getId(), financialInstrument.getName(), financialInstrument.getId());
            financialInstrumentService.update(financialInstrument);
        }
    }

}
