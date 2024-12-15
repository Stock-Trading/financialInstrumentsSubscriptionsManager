package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface FinancialInstrumentRepository {

    Collection<FinancialInstrumentModel> findAllUnassignedToAnyDataLoader();

    boolean existsWithNoDataLoaderAssigned();

    void unassignFromDataLoader(Long dataLoaderId);

}
