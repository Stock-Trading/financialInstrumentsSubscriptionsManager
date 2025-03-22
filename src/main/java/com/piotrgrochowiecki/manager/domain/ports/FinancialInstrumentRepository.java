package com.piotrgrochowiecki.manager.domain.ports;

import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface FinancialInstrumentRepository {

    enum OrderBy {
        CREATED_ON_DESC,
        CREATED_ON_ASC
    }

    FinancialInstrumentModel save(FinancialInstrumentModel financialInstrumentModel);

    Collection<FinancialInstrumentModel> findUnassignedToAnyDataLoader(OrderBy orderBy, Integer limit);

    boolean existsWithNoDataLoaderAssigned();

}
