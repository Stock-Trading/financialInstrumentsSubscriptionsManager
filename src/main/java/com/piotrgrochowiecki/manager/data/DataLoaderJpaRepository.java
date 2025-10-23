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

    @Modifying
    @Query(value = """
            UPDATE DataLoaderEntity dl
            SET dl.active = :activeNewValue,
                dl.loadStatus = :loadStatusNewValue,
                dl.readyForAssignmentOfFinancialInstruments = :readyForAssignmentOfFinancialInstrumentsNewValue
            WHERE
                dl.active = :activeCurrentValue
            AND
                dl.lastConnectedOn < :lastConnectedOn
            """)
    Integer findByActiveAndLastConnectedOnLessThanAndUpdateLoadStatusAndActiveAndReadyForAssignmentOfFinancialInstruments(
            @Param("activeNewValue") Boolean activeNewValue,
            @Param("loadStatusNewValue") Boolean loadStatusNewValue,
            @Param("readyForAssignmentOfFinancialInstrumentsNewValue") Boolean readyForAssignmentOfFinancialInstrumentsNewValue,
            @Param("activeCurrentValue") Boolean activeCurrentValue,
            @Param("lastConnectedOn") Instant lastConnectedOn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout",
                    value = "-2")
    })
    @Query(value = """
            SELECT dl
            FROM DataLoaderEntity dl
            WHERE dl.readyForAssignmentOfFinancialInstruments = true
            """)
    List<DataLoaderEntity> findReadyForAssignmentOfFinancialInstruments(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
    List<DataLoaderEntity> findByLastConnectedOnGreaterThanAndLastInstantOfFinancialInstrumentsAssignmentLessThanEqual(Instant lastConnectedOn,
                                                                                                                       Instant firstHandledOn,
                                                                                                                       Pageable pageable);

    List<DataLoaderEntity> findByLoadStatusAndReadyForAssignmentOfFinancialInstruments(String status,
                                                                                       Boolean readyForAssignmentOfFinancialInstruments,
                                                                                       Pageable pageable);
}
