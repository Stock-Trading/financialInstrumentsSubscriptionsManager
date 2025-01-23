package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collection;

@Repository
public interface DataLoaderRepository {

    DataLoaderModel save(DataLoaderModel dataLoaderModel);

    DataLoaderModel findByUuid(String dataLoaderUUID);

    Collection<DataLoaderModel> find5OldestAndActiveDataLoaders();

    Collection<DataLoaderModel> find5InactiveDataLoaders(Duration timeFromLastConnectionAsHealthThreshold);

    Collection<DataLoaderModel> find5ActiveAndUnhandledDataLoaders(Duration timeFromLastConnectionAsHealthThreshold,
                                                                   Duration timeFromLastHandledTimeAsUnhandledThreshold);

    boolean existsByUuid(String dataLoaderUUID);
}
