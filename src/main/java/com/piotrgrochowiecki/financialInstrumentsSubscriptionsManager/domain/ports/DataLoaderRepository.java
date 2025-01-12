package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface DataLoaderRepository {

    DataLoaderModel save(DataLoaderModel dataLoaderModel);

    void updateDataLoaderLastConnectedOnTime(String DataLoaderUuid, Instant lastConnectedOn);

    DataLoaderModel findByUuid(String dataLoaderUUID);

    Collection<DataLoaderModel> findAllActiveDataLoaders(Duration timeFromLastConnectionAsHealthThreshold);

    Collection<DataLoaderModel> find5InactiveDataLoaders(Duration timeFromLastConnectionAsHealthThreshold);

    Collection<DataLoaderModel> findAllActiveAndUnhandledDataLoaders(Duration timeFromLastConnectionAsHealthThreshold,
                                                                     Duration timeFromLastHandledTimeAsUnhandledThreshold);

    List<DataLoaderModel> findAllWithNumberOfAssignedFinancialInstrumentsGreaterThanRecommendedAndLastConnectedOnGreaterThanOrderAscByFinancialInstrumentCount(Integer recommendedNumberOfFinancialInstrumentsPerDataLoader,
                                                                                                                                                               Duration timeFromLastConnectionAsHealthThreshold);

    List<DataLoaderModel> findAllWithNumberOfAssignedFinancialInstrumentsLessThanEqualRecommendedAndLastConnectedOnGreaterThanOrderAscByFinancialInstrumentCount(Integer recommendedNumberOfFinancialInstrumentsPerDataLoader,
                                                                                                                                                                 Duration timeFromLastConnectionAsHealthThreshold);

    boolean existsByUuid(String dataLoaderUUID);
}
