package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface DataLoaderJpaRepository extends JpaRepository<DataLoaderEntity, Long> {

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

    List<DataLoaderEntity> findByLastConnectedOnGreaterThanEqual(Instant instant);

    List<DataLoaderEntity> findByLastConnectedOnLessThanEqual(Instant instant);

    List<DataLoaderEntity> findByLastConnectedOnGreaterThanAndLastHandledOnLessThanEqual(Instant lastConnectedOn, Instant firstHandledOn);

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
