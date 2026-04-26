package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class UnassignFinancialInstrumentFromDataLoaderWithTooHighLoadStatusUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;

    /**
     * Unassigns excessive financial instruments from overloaded data loaders.
     *
     * <p>
     * <b>Algorithm Flow:</b>
     * </p>
     * <pre>
     * Rebalancing Overloaded Data Loaders (Runs Every 12 Seconds):
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 1: Find Overloaded Data Loaders                    │
     *     │   - Query: Find DLs with loadStatus=TOO_HIGH            │
     *     │   - Filter: AND active=true (only active ones)          │
     *     │   - Order by: LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS     │
     *     │     ASSIGNMENT_ASC (oldest first - fairness)            │
     *     │   - Limit: Configurable batch size                      │
     *     │   - Result: List of IDs for overloaded DLs              │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ├─ If list is empty:
     *                        │    No overloaded loaders
     *                        │    No rebalancing needed, return
     *                        │
     *                        └─ If list has items: Process each
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 2: For Each Overloaded Data Loader:                │
     *     │   Process found overloaded loaders one by one           │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2a: Get All Assigned Financial Instruments         │
     *     │   - Query: Find all FIs with dataLoaderId = DL.id       │
     *     │   - Result: Complete list of assigned FIs               │
     *     │   - Count: Total number of assigned FIs                 │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2b: Calculate Excess Load                          │
     *     │   - excess = total_assigned - recommended_count         │
     *     │   - If excess <= 0: No action needed (balanced)         │
     *     │   - If excess > 0: Must unassign the excess             │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *        ┌───────────────┴───────────────┐
     *        │                               │
     *    excess <= 0                     excess > 0
     *     │                               │
     *     ▼                               ▼
     *
     *  No Action                    ┌──────────────────────────┐
     *  Skip DL                      │ Proceed to Unassignment  │
     *  Move to next                 └──────────────────────────┘
     *                                       │
     *                                       ▼
     *                        ┌─────────────────────────────────────┐
     *                        │ Step 2c: Identify Excess FIs to Keep│
     *                        │   - Skip first N FIs where:         │
     *                        │     N = recommended count           │
     *                        │   - These N FIs stay assigned       │
     *                        │   - All remaining will be unassign  │
     *                        └──────────────────┬──────────────────┘
     *                                           │
     *                                           ▼
     *                        ┌─────────────────────────────────────┐
     *                        │ Step 2d: Collect Excess FI IDs      │
     *                        │   - Extract IDs from FIs to remove  │
     *                        │   - Build list of excess FI IDs     │
     *                        │   - Count: excess = total - keep    │
     *                        └──────────────────┬──────────────────┘
     *                                           │
     *                                           ▼
     *                        ┌─────────────────────────────────────┐
     *                        │ Step 2e: Batch Unassign Excess FIs  │
     *                        │   - Execute SQL batch update:       │
     *                        │     SET dataLoaderId = null         │
     *                        │     WHERE id IN (excess FI IDs)     │
     *                        │   - Result: Count of unassigned     │
     *                        └──────────────────┬──────────────────┘
     *                                           │
     *                                           ▼
     *                        ┌─────────────────────────────────────┐
     *                        │ Step 2f: Log Rebalancing Results    │
     *                        │   - Unassigned X FIs from DL_Y      │
     *                        │   - Example: "Unassigned 4 FIs"     │
     *                        │   - Move to next overloaded DL      │
     *                        └──────────────────┬──────────────────┘
     *                                           │
     *                                           ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ COMPLETE: All Overloaded DLs Rebalanced                 │
     *     │   - Excess FIs now UNASSIGNED (dataLoaderId=null)       │
     *     │   - Ready for reassignment by Assignment Task (10s)     │
     *     │   - System converges to balanced state                  │
     *     └─────────────────────────────────────────────────────────┘
     * </pre>
     *
     * <p>
     * <b>Excess Calculation - Detailed Examples:</b>
     * </p>
     * <table border="1">
     *   <tr>
     *     <th>Recommended</th>
     *     <th>Currently Assigned</th>
     *     <th>Excess Calc</th>
     *     <th>Action</th>
     *     <th>Result</th>
     *   </tr>
     *   <tr>
     *     <td>5</td>
     *     <td>5</td>
     *     <td>5 - 5 = 0</td>
     *     <td>No action (balanced)</td>
     *     <td>Stays: 5, Unassigned: 0</td>
     *   </tr>
     *   <tr>
     *     <td>5</td>
     *     <td>8</td>
     *     <td>8 - 5 = 3</td>
     *     <td>Unassign last 3 FIs</td>
     *     <td>Stays: 5, Unassigned: 3</td>
     *   </tr>
     *   <tr>
     *     <td>5</td>
     *     <td>12</td>
     *     <td>12 - 5 = 7</td>
     *     <td>Unassign last 7 FIs</td>
     *     <td>Stays: 5, Unassigned: 7</td>
     *   </tr>
     * </table>
     *
     * <p>
     * <b>Fairness Strategy - Processing Order:</b>
     * </p>
     * <pre>
     * Data loaders are processed by:
     * LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC
     *
     * Meaning: Process loaders that have had their FIs assigned LONGEST time ago
     *
     * Example:
     *   DL_1: Last assignment @ t=0s (oldest)   ← Process FIRST
     *   DL_2: Last assignment @ t=5s
     *   DL_3: Last assignment @ t=8s (newest)   ← Process LAST
     *
     * Benefit:
     *   - Loaders with longest-running imbalance get priority
     *   - Recently overloaded loaders get time to self-balance
     *   - Fair distribution of rebalancing work
     * </pre>
     *
     * <p>
     * <b>Key Responsibilities:</b>
     * </p>
     * <ul>
     *   <li>Find all active data loaders with TOO_HIGH load status</li>
     *   <li>Order them by oldest assignment time to ensure fairness</li>
     *   <li>For each loader, calculate how many FIs exceed recommended capacity</li>
     *   <li>Keep only the recommended number of oldest FIs</li>
     *   <li>Unassign all excess financial instruments</li>
     *   <li>Log all unassignments for monitoring</li>
     * </ul>
     *
     * <p>
     * <b>Execution Context:</b>
     * </p>
     * <ul>
     *   <li>Executed by {@link com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentBalancingSchedulerService}</li>
     *   <li>Transactional - ensures unassignments are atomic</li>
     *   <li>Depends on: Load status already determined (STEP 3 runs before this)</li>
     *   <li>Precedes: Assignment task (awaits freed FIs for reassignment in next cycle)</li>
     * </ul>
     */

    @Transactional
    public void unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus() {
        log.info("Retrieving IDs of Data Loaders with Too High status and Active flag set to true");
        List<Long> idsOfDataLoadersWithTooHighBalance = dataLoaderRepository.findIdByLoadStatusAndActive(
                        DataLoaderModel.Status.TOO_HIGH,
                        true,
                        DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC,
                        dataLoaderParametersProvider.getNumberOfDataLoadersHandledByManagerInOneCycle())
                .stream()
                .toList();
        log.debug("Ids of Data Loaders with load status Too High and Active flag set to true: {}",
                Arrays.toString(idsOfDataLoadersWithTooHighBalance.toArray()));
        for (Long id : idsOfDataLoadersWithTooHighBalance) {
            unassignFinancialInstrumentsFromDataLoader(id);
        }
    }

    private void unassignFinancialInstrumentsFromDataLoader(Long idOfDataLoader) {
        log.debug("Unassigning excessive Financial Instruments from Data Loader with id {}", idOfDataLoader);
        List<FinancialInstrumentModel> financialInstrumentModels =
                financialInstrumentRepository.findByDataLoaderId(idOfDataLoader);
        log.debug("Number of Financial Instruments assigned to Data Loader id={} is {}",
                idOfDataLoader, financialInstrumentModels.size());

        //don't unassign more FIs from DL than needed:
        int numberOfFinancialInstrumentsToSkip =
                dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader();
        log.debug("Recommended number of Financial Instruments per Data Loader is set to {}",
                numberOfFinancialInstrumentsToSkip);
        List<Long> financialInstrumentModelsIdsToNullifyDataLoaders = financialInstrumentModels.stream()
                .skip(numberOfFinancialInstrumentsToSkip)
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
