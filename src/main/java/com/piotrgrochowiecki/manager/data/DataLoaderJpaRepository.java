package com.piotrgrochowiecki.manager.data;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface DataLoaderJpaRepository extends JpaRepository<DataLoaderEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
        //jakarta.persistence.lock.timeout=-2 sets up SELECT FOR UPDATE
    Optional<DataLoaderEntity> findByUuid(String uuid);

    boolean existsByUuid(String uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout",
                    value = "-2")
    })
    @Query(value = """
            SELECT dl
            FROM DataLoaderEntity dl
            WHERE dl.active = true
            """)
    List<DataLoaderEntity> findAllActive(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout",
                    value = "-2")
    })
    List<DataLoaderEntity> findByActiveAndLastConnectedOnLessThan(Boolean activeStatus,
                                                                  Instant lastConnectedOn,
                                                                  Pageable pageable);

    /**
     * Sets <i>Active</i> flag to <b>false</b> and <i>LoadStatus</i> to <b>null</b> of DataLoaders that are active (=true),
     * but they have not connected to the service in specified threshold.
     *
     * @param lastConnectedOn Instant of last time when Data Loader connected to the service
     * @return number of updated rows
     */
    @Modifying(flushAutomatically = true,
            clearAutomatically = true)
    @Query(value = """
            UPDATE DataLoaderEntity dl
            SET dl.active = false,
                dl.loadStatus = null,
                dl.lastLoadStatusUpdate = CURRENT_TIMESTAMP
            WHERE
                dl.active = true
            AND
                dl.lastConnectedOn < :lastConnectedOn
            """)
    Integer setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(
            @Param("lastConnectedOn") Instant lastConnectedOn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
    List<DataLoaderEntity> findByLastConnectedOnGreaterThanAndLastInstantOfFinancialInstrumentsAssignmentLessThanEqual(Instant lastConnectedOn,
                                                                                                                       Instant firstHandledOn,
                                                                                                                       Pageable pageable);

    List<DataLoaderEntity> findByLoadStatusAndActive(String status,
                                                     Boolean active,
                                                     Pageable pageable);
}
