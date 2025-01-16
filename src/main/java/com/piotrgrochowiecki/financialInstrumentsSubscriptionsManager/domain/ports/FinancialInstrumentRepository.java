package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface FinancialInstrumentRepository {

    FinancialInstrumentModel save(FinancialInstrumentModel financialInstrumentModel);

    Collection<FinancialInstrumentModel> find5UnassignedToAnyDataLoader();

    boolean existsWithNoDataLoaderAssigned();

}
