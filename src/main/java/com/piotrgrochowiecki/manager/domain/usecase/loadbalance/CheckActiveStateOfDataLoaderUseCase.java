package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * <p>
 * Use case responsible for monitoring the health status of data loaders through periodic check-in validation.
 * </p>
 *
 * <p>
 * <b>Algorithm Flow:</b>
 * </p>
 * <pre>
 * Active State Check (Runs Every 3 Seconds):
 *
 *     ┌─────────────────────────────────────────────────────────┐
 *     │ STEP 1: Calculate Timeout Threshold                     │
 *     │   - Get current UTC time                                │
 *     │   - Subtract: ACTIVE_THRESHOLD_MS (config parameter)    │
 *     │   - Result: Threshold timestamp                         │
 *     └──────────────────┬──────────────────────────────────────┘
 *                        │
 *                        │ Example:
 *                        │ Now:       2026-04-26 12:00:00 UTC
 *                        │ Threshold: 30,000 ms (30 seconds)
 *                        │ Cutoff:    2026-04-26 11:59:30 UTC
 *                        │
 *                        ▼
 *
 *     ┌─────────────────────────────────────────────────────────┐
 *     │ STEP 2: Query for Inactive Data Loaders                 │
 *     │   - Find all DLs where:                                 │
 *     │     * lastCheckin < threshold_timestamp                 │
 *     │     * This means they haven't checked in within 30s     │
 *     └──────────────────┬──────────────────────────────────────┘
 *                        │
 *                        ├─ If count == 0:
 *                        │    All loaders are responsive
 *                        │    Log debug message, return
 *                        │
 *                        └─ If count > 0: Found inactive loaders
 *
 *     ┌─────────────────────────────────────────────────────────┐
 *     │ STEP 3: Mark Inactive Loaders                           │
 *     │   For each timed-out Data Loader:                       │
 *     │     - Set active = false                                │
 *     │     - Set loadStatus = null                             │
 *     │     - Persist to database                               │
 *     │   Result: Count of updated loaders                      │
 *     └──────────────────┬──────────────────────────────────────┘
 *                        │
 *                        ▼
 *
 *     ┌─────────────────────────────────────────────────────────┐
 *     │ COMPLETE: Database Updated                              │
 *     │   - Inactive loaders now have active=false              │
 *     │   - Load status cleared for recovery detection          │
 *     │   - Next cleanup cycle will unassign their FIs          │
 *     │   - Followed by reassignment to active loaders          │
 *     └─────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>
 * <b>Timeout Configuration:</b>
 * </p>
 * <pre>
 * Configuration Parameter: ACTIVE_THRESHOLD_MS (milliseconds)
 *
 *     Recommended value: 30,000 ms (30 seconds)
 *
 *     Timeline Example:
 *       t=0s    DL_1 checks in successfully (lastCheckin = 0s)
 *       t=15s   DL_1 still responsive (no check-in needed yet)
 *       t=30s   Active state check runs
 *       t=30s   Checks: lastCheckin (0s) < threshold (0s)?
 *       t=30s   NO : 0s is NOT older than threshold
 *       t=30s   Decision: DL_1 marked as ACTIVE
 *       
 *       t=35s   DL_2 hasn't checked in since t=0s
 *       t=35s   DL_2.lastCheckin (0s) is 35s old
 *       t=35s   Active state check: lastCheckin < threshold?
 *       t=35s   YES: 0s IS older than (now - 30s)
 *       t=35s   Decision: DL_2 marked as INACTIVE
 * </pre>
 *
 * <p>
 * <b>Key Responsibilities:</b>
 * </p>
 * <ul>
 *   <li>Calculate active threshold timestamp based on configured timeout</li>
 *   <li>Query database for data loaders that haven't checked in within threshold</li>
 *   <li>Mark timed-out loaders as inactive</li>
 *   <li>Clear load status of inactive loaders</li>
 *   <li>Log the number of affected data loaders</li>
 * </ul>
 *
 * <p>
 * <b>Execution Context:</b>
 * </p>
 * <ul>
 *   <li>Executed by {@link com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentBalancingSchedulerService}</li>
 *   <li>Transactional - persists changes to database</li>
 *   <li>Depends on: TimeService for current UTC time</li>
 *   <li>Precedes: Cleanup task (detects and unassigns FIs from inactive DLs)</li>
 * </ul>
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class CheckActiveStateOfDataLoaderUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;
    private final TimeService timeService;

    /**
     * Checks for last check-in time of DataLoader. If that time is longer then specified threshold,
     * unassigns all Financial Instrument from it and sets its Active property to false.
     */
    @Transactional
    public void checkActiveState() {
        Instant lastInstantCountingAsActive = timeService.getInstantUTC()
                .minus(Duration.ofMillis(dataLoaderParametersProvider.getActiveThresholdMilliseconds()));

        Integer numberOfUpdatedModels = dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(
                lastInstantCountingAsActive);

        log.debug("Number of updated data loaders: {}", numberOfUpdatedModels);
    }
}
