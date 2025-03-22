package com.piotrgrochowiecki.manager.domain.ports;

import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface FinancialInstrumentRepository {

    FinancialInstrumentModel save(FinancialInstrumentModel financialInstrumentModel);

    Collection<FinancialInstrumentModel> find5UnassignedToAnyDataLoader();

    boolean existsWithNoDataLoaderAssigned();

}
