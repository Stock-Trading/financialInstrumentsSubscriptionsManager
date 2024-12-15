package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface FinancialInstrumentJpaRepository extends JpaRepository<FinancialInstrumentEntity, Long> {

    FinancialInstrumentEntity getByName(String name);

    FinancialInstrumentEntity getBySymbol(String symbol);

    @Query(value = "SELECT fi.symbol " +
            "FROM financial_instrument fi",
            nativeQuery = true)
    List<String> getAllSymbols();

    List<FinancialInstrumentEntity> findByDataLoaderId(Long id);

    List<FinancialInstrumentEntity> findByDataLoaderUuid(String dataLoaderUuid);

    @Query(value = "SELECT fi.* " +
            "FROM financial_instrument fi " +
            "WHERE fi.data_loader_uuid IS NULL " +
            "AND fi.data_loader_id IS NULL",
            nativeQuery = true)
    List<FinancialInstrumentEntity> findUnassignedToAnyDataLoader();

    void deleteById(Long id);

    void deleteByName(String name);

    void deleteBySymbol(String symbol);

    boolean existsByName(String name);

    boolean existsBySymbol(String symbol);

    @Query(value = "SELECT EXISTS(SELECT 1) " +
            "FROM financial_instrument f " +
            "WHERE f.data_loader_uuid IS NULL",
            nativeQuery = true)
    boolean existsWithNoDataLoaderAssigned();

    @Modifying
    @Transactional
    @Query(value = "UPDATE financial_instrument fi " +
            "SET data_loader_id = NULL, data_loader_uuid = NULL " +
            "WHERE fi.data_loader_id = :dataLoaderId " +
            "AND fi.id IN (SELECT id FROM financial_instrument " +
            "WHERE data_loader_id = :dataLoaderId " +
            "FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    void unassignFromDataLoader(@Param("dataLoaderId") Long dataLoaderId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE financial_instrument fi " +
            "SET fi.data_loader_id = :dataLoaderId, fi.data_loader_uuid = :dataLoaderUuid " +
            "WHERE fi.id IN (SELECT id FROM financial_instrument " +
            "WHERE id = :financialInstrumentId" +
            " FOR UPDATE SKIP LOCKED)",
            nativeQuery = true)
    void assignToDataLoader(@Param("dataLoaderId") Long dataLoaderId,
                            @Param("dataLoaderUuid") String dataLoaderUuid,
                            @Param("financialInstrumentId") Long financialInstrumentId);

}