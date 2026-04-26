package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import com.piotrgrochowiecki.manager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class CheckDataLoaderLoadStatusUseCase {

    private final DataLoaderParametersProvider dataLoaderParametersProvider;
    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final DataLoaderService dataLoaderService;
    private final TimeService timeService;

    /**
     * Checks and updates the load status of active data loaders.
     *
     * <p>
     * <b>Algorithm Flow:</b>
     * </p>
     * <pre>
     * Load Status Assessment:
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 1: Retrieve Active Data Loaders                    │
     *     │   - Query all DLs with active=true                      │
     *     │   - Order by: LAST_LOAD_STATUS_UPDATE_ASC               │
     *     │     (oldest update first - ensures fairness)            │
     *     │   - Limit: Configurable batch size                      │
     *     │   - Result: List of active Data Loaders                 │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ STEP 2: For Each Active Data Loader:                    │
     *     │   Process all loaders in the retrieved batch            │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2a: Count Assigned Financial Instruments           │
     *     │   - Query database for FIs with:                        │
     *     │     * dataLoaderId = this DL's ID                       │
     *     │   - Get total count of assigned FIs                     │
     *     │   - Result: Long numberOfAssignedFIs                    │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2b: Determine Load Status                          │
     *     │   Compare: numberOfAssignedFIs vs RECOMMENDED_COUNT     │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *        ┌───────────────┼───────────────┐
     *        │               │               │
     *        ▼               ▼               ▼
     *
     *    BALANCED       TOO_LOW           TOO_HIGH
     *    (== N)         (< N)             (> N)
     *    0 action       Ready to receive  Send away excess
     *    needed         more FIs          FIs
     *
     *        │               │               │
     *        └───────────────┼───────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ Step 2c: Update Timestamps and Persist                  │
     *     │   - Set: loadStatus = calculated status                 │
     *     │   - Set: lastLoadStatusUpdate = now (UTC)               │
     *     │   - Persist: Save Data Loader to database               │
     *     │   - Note: lastLoadStatusUpdate allows other tasks       │
     *     │           to find "oldest" update for next cycle        │
     *     └──────────────────┬──────────────────────────────────────┘
     *                        │
     *                        ▼
     *
     *     ┌─────────────────────────────────────────────────────────┐
     *     │ COMPLETE: All Active DLs Assessed and Updated           │
     *     │   - All load statuses are current                       │
     *     │   - Ready for assignment and rebalancing tasks          │
     *     │   - Next cycle will prioritize the oldest updates       │
     *     └─────────────────────────────────────────────────────────┘
     * </pre>
     *
     * <p>
     * <b>Load Status Determination - Detailed Examples:</b>
     * </p>
     * <table border="1">
     *   <tr>
     *     <th>Recommended</th>
     *     <th>Assigned FIs</th>
     *     <th>Comparison</th>
     *     <th>Status</th>
     *     <th>Next Action</th>
     *   </tr>
     *   <tr>
     *     <td>5</td>
     *     <td>0-4</td>
     *     <td>&lt; 5</td>
     *     <td>TOO_LOW</td>
     *     <td>Assign Task (10s) will add FIs here</td>
     *   </tr>
     *   <tr>
     *     <td>5</td>
     *     <td>5</td>
     *     <td>== 5</td>
     *     <td>BALANCED</td>
     *     <td>No action needed, stable state</td>
     *   </tr>
     *   <tr>
     *     <td>5</td>
     *     <td>6-10+</td>
     *     <td>&gt; 5</td>
     *     <td>TOO_HIGH</td>
     *     <td>Unassign Task (12s) will remove excess</td>
     *   </tr>
     * </table>
     *
     * <p>
     * <b>Key Responsibilities:</b>
     * </p>
     * <ul>
     *   <li>Fetch active data loaders in order of oldest status update</li>
     *   <li>Count financial instruments assigned to each loader</li>
     *   <li>Determine appropriate status based on load metrics</li>
     *   <li>Update last status update timestamp for load balancing priority</li>
     *   <li>Persist updated data loaders with new load status</li>
     * </ul>
     *
     * <p>
     * <b>Execution Context:</b>
     * </p>
     * <ul>
     *   <li>Executed by {@link com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentBalancingSchedulerService}</li>
     *   <li>Transactional - ensures data consistency</li>
     *   <li>Depends on: Active state already determined (STEP 2 runs first)</li>
     *   <li>Precedes: Assignment task (uses TOO_LOW status) and Unassign task (uses TOO_HIGH status)</li>
     * </ul>
     */
    @Transactional
    public void checkLoadStatus() {
        log.info("Retrieving Data Loaders to check their load status");
        List<DataLoaderModel> dataLoaderModelList = dataLoaderRepository.findActiveDataLoaders(
                        DataLoaderRepository.OrderBy.LAST_LOAD_STATUS_UPDATE_ASC,
                        dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
                .stream()
                .toList();
        log.info("List of Data Loaders contains {} objects", dataLoaderModelList.size());
        dataLoaderModelList.forEach(this::checkAndUpdateLoadStatus);
    }

    private void checkAndUpdateLoadStatus(DataLoaderModel dataLoader) {
        log.info("Checking load status of dataLoader {}", dataLoader.toString());
        long numberOfAssignedFinancialInstruments = financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(
                dataLoader.getId());
        log.info("Number of assigned Financial Instruments: {}", numberOfAssignedFinancialInstruments);
        DataLoaderModel.Status loadStatus = getStatus(numberOfAssignedFinancialInstruments);
        dataLoader.setLoadStatus(loadStatus);
        dataLoader.setLastLoadStatusUpdate(timeService.getInstantUTC()); //updates time of last time
        // of Load Status flag update, so other instances of this service can retrieve records with "oldest"
        // lastLoadStatusUpdate
        log.debug("""
                        Data Loader with id {}, uuid {} has {} Financial Instruments assigned to it and its load status is {}.
                        Number of recommended Financial Instruments per Data Loader is {}.
                        """,
                dataLoader.getId(),
                dataLoader.getUuid(),
                numberOfAssignedFinancialInstruments,
                loadStatus,
                dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader());
        dataLoaderService.update(dataLoader);
    }

    private DataLoaderModel.Status getStatus(long numberOfAssignedFinancialInstruments) {
        DataLoaderModel.Status loadStatus;
        if (numberOfAssignedFinancialInstruments == dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
            loadStatus = DataLoaderModel.Status.BALANCED;
        } else if (numberOfAssignedFinancialInstruments < dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
            loadStatus = DataLoaderModel.Status.TOO_LOW;
        } else {
            loadStatus = DataLoaderModel.Status.TOO_HIGH;
        }
        return loadStatus;
    }

}
