package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface DataLoaderJpaRepository extends JpaRepository<DataLoaderEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DataLoaderEntity> findByUuid(String uuid);

    @Query(value = "SELECT COUNT(d) " +
            "FROM DataLoaderEntity d",
            nativeQuery = true)
    long countDataLoaders();

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE data_loader dl " +
            "SET dl.last_connected_on = :instant " +
            "WHERE dl.uuid = :uuid",
            nativeQuery = true)
    void updateDataLoaderLastConnectedOnTime(@Param("uuid") String dataLoaderUuid, @Param("instant") Instant instant);

    boolean existsByUuid(String uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
    List<DataLoaderEntity> findTop5ByActiveTrueOrderByLastConnectedOnAsc();

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

    @Query(value = "SELECT * " +
            "FROM data_loader dl " +
            "COUNT(fi.id) AS instrumentCount " +
            "JOIN financial_instrument fi ON data_loader dl = dl.id " +
            "GROUP BY dl.id, dl.uuid " +
            "HAVING COUNT(fi.id) > :instrumentThreshold " +
            "ORDER BY instrumentCount ASC",
            nativeQuery = true
    )
    List<DataLoaderEntity> findAllWithNumberOfAssignedFinancialInstrumentsGreaterThanRecommendedOrderAsc(@Param("instrumentThreshold") Integer numberOfRecommendedFinancialInstrumentsPerDataLoader);

    @Query(value = "SELECT * " +
            "FROM data_loader dl " +
            "COUNT(fi.id) AS instrumentCount " +
            "JOIN financial_instrument fi ON data_loader dl = dl.id " +
            "WHERE dl.last_connected_on > :lastConnectedThreshold " +
            "GROUP BY dl.id, dl.uuid, dl.last_connected_on " +
            "HAVING COUNT(fi.id) <= :instrumentThreshold " +
            "ORDER BY instrumentCount ASC",
            nativeQuery = true
    )
    List<DataLoaderEntity> findAllWithNumberOfAssignedFinancialInstrumentsLessThanEqualRecommendedAndLastConnectedOnGreaterThanOrderAscByFinancialInstrumentCount(@Param("instrumentThreshold") Integer numberOfRecommendedFinancialInstrumentsPerDataLoader,
                                                                                                                                                                  @Param("lastConnectedThreshold") Instant lastConnectedOn);

}
