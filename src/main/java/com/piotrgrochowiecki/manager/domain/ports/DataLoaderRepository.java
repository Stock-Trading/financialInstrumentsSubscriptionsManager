package com.piotrgrochowiecki.manager.domain.ports;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collection;

@Repository
public interface DataLoaderRepository {

    enum OrderBy {
        LAST_CONNECTED_ON_DESC,
        LAST_CONNECTED_ON_ASC
    }

    DataLoaderModel save(DataLoaderModel dataLoaderModel);

    DataLoaderModel findByUuid(String dataLoaderUUID);

    Collection<DataLoaderModel> findActiveDataLoaders(OrderBy orderBy, int limit);

    Collection<DataLoaderModel> findReadyForHandling(OrderBy orderBy, int limit);

    Collection<DataLoaderModel> findActiveAndUnhandledDataLoaders(Duration timeFromLastConnectionAsHealthThreshold,
                                                                  Duration timeFromLastHandledTimeAsUnhandledThreshold,
                                                                  OrderBy orderBy,
                                                                  int limit);

    boolean existsByUuid(String dataLoaderUUID);
}
