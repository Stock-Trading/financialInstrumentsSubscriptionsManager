package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;

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
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
    List<DataLoaderEntity> findTop5ByReadyForHandlingTrueOrderByLastConnectedOnAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
    List<DataLoaderEntity> findTop5ByLastConnectedOnLessThan(Instant instant);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
    List<DataLoaderEntity> findTop5ByLastConnectedOnGreaterThanAndLastHandledOnLessThanEqual(Instant lastConnectedOn,
                                                                                             Instant firstHandledOn);

}
