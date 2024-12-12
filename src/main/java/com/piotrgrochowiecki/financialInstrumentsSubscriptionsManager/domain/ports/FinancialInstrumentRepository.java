package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialInstrumentRepository {

    Optional<FinancialInstrumentModel> findById(Long id);

    Optional<FinancialInstrumentModel> findByName(String name);

    Optional<FinancialInstrumentModel> findBySymbol(String symbol);

    List<FinancialInstrumentModel> findAll();

    Collection<FinancialInstrumentModel> findAllAssignedToDataLoaderByUuid(String dataLoaderUuid);

    Collection<FinancialInstrumentModel> findAllUnassignedToAnyDataLoader();

    List<String> findAllSymbols();

    FinancialInstrumentModel save(FinancialInstrumentModel financialInstrumentModel);

    void deleteById(Long id);

    void deleteByName(String name);

    void deleteBySymbol(String symbol);

    boolean existsById(Long id);

    boolean existsByName(String name);

    boolean existsBySymbol(String symbol);

    boolean existsWithNoDataLoaderAssigned();

    void unassignFromDataLoader(Long dataLoaderId);

    void assignToDataLoader(Long dataLoaderId, String dataLoaderUuid, Long financialInstrumentId);
}
