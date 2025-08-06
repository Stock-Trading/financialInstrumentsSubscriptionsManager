package com.piotrgrochowiecki.manager.domain.port;

import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FinancialInstrumentRepository {

    enum OrderBy {
        CREATED_ON_DESC,
        CREATED_ON_ASC
    }

    FinancialInstrumentModel save(FinancialInstrumentModel financialInstrumentModel);

    Collection<FinancialInstrumentModel> findUnassignedToAnyDataLoader(OrderBy orderBy, int limit);

    boolean existsWithNoDataLoaderAssigned();

    FinancialInstrumentModel detachDataLoader(FinancialInstrumentModel financialInstrumentModel);

    FinancialInstrumentModel attachDataLoaderById(FinancialInstrumentModel financialInstrumentModel, Long dataLoaderId);

    List<FinancialInstrumentModel> findByDataLoaderId(Long dataLoaderId);
}
