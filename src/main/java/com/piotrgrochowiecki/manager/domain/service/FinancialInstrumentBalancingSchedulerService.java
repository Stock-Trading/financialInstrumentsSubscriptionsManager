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
 * <h2>High-Level Algorithm Overview</h2>
 *
 * <p>
 * The algorithm implements a five-phase continuous load balancing cycle:
 * </p>
 *
 * <pre>
 * Five Concurrent Scheduled Tasks (Running Independently):
 *
 *     Every 1 second (CRITICAL PRIORITY):
 *     [1] Cleanup: Unassign FIs from Inactive Data Loaders
 *         └─ Immediate failure detection and recovery
 *
 *     Every 3 seconds (HIGH PRIORITY):
 *     [2] Health Check: Update Active Status of Data Loaders
 *         └─ Mark non-responsive loaders as inactive
 *
 *     Every 5 seconds (HIGH PRIORITY):
 *     [3] Load Assessment: Calculate Load Status
 *         └─ Determine: BALANCED / TOO_LOW / TOO_HIGH
 *
 *     Every 10 seconds (MEDIUM PRIORITY):
 *     [4] Distribution: Assign Unassigned FIs to Available DLs
 *         └─ Uses round-robin algorithm for fair distribution
 *
 *     Every 12 seconds (MEDIUM PRIORITY):
 *     [5] Rebalancing: Unassign Excess FIs from Overloaded DLs
 *         └─ Brings overloaded loaders back to balanced state
 *
 * All tasks run asynchronously and continuously.
 * System converges to balanced state within 12 seconds of any change.
 * </pre>
 *
 * <h2>Execution Schedule</h2>
 *
 * <p>
 * The algorithm executes the following methods at defined intervals (all running continuously and independently):
 * </p>
 * <table border="1">
 *   <tr>
 *     <th>Interval</th>
 *     <th>Priority</th>
 *     <th>Method</th>
 *     <th>Purpose</th>
 *   </tr>
 *   <tr>
 *     <td>Every 1 second</td>
 *     <td>CRITICAL</td>
 *     <td>unassignFinancialInstrumentsFromInactiveDataLoaders()</td>
 *     <td>Clean up from failed data loaders immediately</td>
 *   </tr>
 *   <tr>
 *     <td>Every 3 seconds</td>
 *     <td>HIGH</td>
 *     <td>checkActiveState()</td>
 *     <td>Determine which data loaders are still responsive</td>
 *   </tr>
 *   <tr>
 *     <td>Every 5 seconds</td>
 *     <td>HIGH</td>
 *     <td>checkLoadStatus()</td>
 *     <td>Calculate load for each data loader</td>
 *   </tr>
 *   <tr>
 *     <td>Every 10 seconds</td>
 *     <td>MEDIUM</td>
 *     <td>assignUnassignedInstrumentsToActiveDataLoaders()</td>
 *     <td>Distribute free instruments to available capacity</td>
 *   </tr>
 *   <tr>
 *     <td>Every 12 seconds</td>
 *     <td>MEDIUM</td>
 *     <td>unassignFinancialInstrumentsFromDataLoadersWithTooHighLoadStatus()</td>
 *     <td>Rebalance away from overloaded data loaders</td>
 *   </tr>
 * </table>
 *
 * <h2>State Transitions</h2>
 *
 * <pre>
 * Financial Instrument Lifecycle:
 *
 *     ┌─────────────────────────────────────────────────────┐
 *     │              UNASSIGNED STATE                       │
 *     │          (dataLoaderId = null)                      │
 *     │                                                     │
 *     │     No active data loader owns this instrument      │
 *     └──────────────────┬──────────────────────────────────┘
 *                        │
 *                        │ [ASSIGN TASK - Every 10s]
 *                        │ Grab from unassigned pool
 *                        │ Assign to active DL (round-robin)
 *                        ▼
 *     ┌─────────────────────────────────────────────────────┐
 *     │              ASSIGNED STATE                         │
 *     │          (dataLoaderId = DL_ID)                     │
 *     │                                                     │
 *     │     Data loader actively monitors this instrument   │
 *     └──────────────┬──────────────────────┬───────────────┘
 *                    │                      │
 *     ┌──────────────┘                      └──────────────┐
 *     │                                                    │
 *     │ [CLEANUP - Every 1s]       [UNASSIGN - Every 12s]  │
 *     │ DL became inactive         DL overloaded (TOO_HIGH)│
 *     │ Emergency unassignment     Rebalance away excess   │
 *     │                                                    │
 *     └──────────────┬─────────────────────┬───────────────┘
 *                    │                     │
 *                    └──────────┬──────────┘
 *                               ▼
 *                    Back to UNASSIGNED
 *                    Ready for reassignment
 * </pre>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Independent Execution:</b> All 5 tasks run asynchronously. No task blocks another.</li>
 *   <li><b>Rapid Recovery:</b> Failures detected within 1 second, FIs reassigned within 10 seconds total.</li>
 *   <li><b>Fair Distribution:</b> Round-robin assignment and ordered processing ensure no starvation.</li>
 *   <li><b>Automatic Self-Healing:</b> System naturally converges to balanced state without manual intervention.</li>
 *   <li><b>Scalable Batching:</b> Batch processing prevents O(n²) behavior as cluster grows.</li>
 * </ul>
 *
 * <h2>Failure Recovery Example</h2>
 *
 * <p>
 * Scenario: A data loader dies while holding 3 financial instruments.
 * </p>
 * <pre>
 * Timeline of Automatic Recovery:
 *
 *     t=0s  ├─ DATA LOADER FAILURE
 *           │  DL_2 process crashes or becomes unresponsive
 *           │  Last checkin timestamp now stale
 *           │
 *     t=1s  ├─ [CLEANUP TASK RUNS - Every 1s]
 *           │  Nothing detected yet (active flag still true)
 *           │  Waits for active state check
 *           │
 *     t=3s  ├─ [ACTIVE STATE CHECK - Every 3s]
 *           │  Compares lastCheckin against threshold
 *           │  Detects: "lastCheckin too old"
 *           │  Action: Sets active = false, loadStatus = null
 *           │
 *     t=4s  ├─ [CLEANUP TASK RUNS AGAIN - Every 1s]
 *           │  Detects: DL_2 now has active=false
 *           │  Finds all 3 FIs assigned to DL_2
 *           │  Action: Unassigns all 3 FIs (dataLoaderId = null)
 *           │  Result: All 3 FIs now UNASSIGNED and ready
 *           │
 *     t=10s ├─ [ASSIGNMENT TASK RUNS - Every 10s]
 *           │  Finds 3 unassigned FIs
 *           │  Finds active DLs with available capacity
 *           │  Action: Assigns using round-robin algorithm
 *           │  FI_1 → DL_3, FI_2 → DL_4, FI_3 → DL_3
 *           │
 *           └─ COMPLETE: All instruments reassigned
 *
 * Recovery Metrics:
 *   - Failure Detection:  3 seconds   (t=3s - t=0s)
 *   - Cleanup Time:       1 second    (t=4s - t=3s)
 *   - Full Recovery:      10 seconds  (t=10s - t=0s)
 *   - Manual Action Needed: NONE (fully automatic)
 * </pre>
 *
 * <h2>Documentation References</h2>
 *
 * For detailed algorithm documentation, see:
 * <ul>
 *   <li><b>{@code LOAD_BALANCING_ALGORITHM.md}</b> - Comprehensive technical guide with detailed workflows</li>
 *   <li><b>Individual Use Case Classes</b> - Each has Mermaid diagrams of internal logic</li>
 * </ul>
 *
 * <p>
 * <b>Key Responsibilities:</b>
 * </p>
 * <ul>
 *   <li>Ensure high availability through continuous health checks and rebalancing</li>
 *   <li>Maintain balanced load distribution across all active data loaders</li>
 *   <li>React to changes in data loader status and capacity</li>
 *   <li>Automatically recover from data loader failures by reassigning instruments</li>
 *   <li>Scale gracefully with cluster size changes</li>
 * </ul>
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
