package com.piotrgrochowiecki.manager.domain.port;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;

@Repository
public interface DataLoaderRepository {

    enum OrderBy {
        LAST_CONNECTED_ON_DESC,
        LAST_CONNECTED_ON_ASC,
        LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_DESC,
        LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC,
        LAST_LOAD_STATUS_UPDATE_DESC,
        LAST_LOAD_STATUS_UPDATE_ASC
    }

    DataLoaderModel save(DataLoaderModel dataLoaderModel);

    DataLoaderModel findByUuid(String dataLoaderUUID);

    Collection<DataLoaderModel> findActiveDataLoaders(OrderBy orderBy, int limit);

    Integer setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(Instant lastConnectedOn);

//    Collection<DataLoaderModel> findActiveAndUnhandledDataLoaders(Duration timeFromLastConnectionAsHealthThreshold,
//                                                                  Duration timeFromLastHandledTimeAsUnhandledThreshold,
//                                                                  OrderBy orderBy,
//                                                                  int limit);

    Collection<DataLoaderModel> findByLoadStatusAndActive(DataLoaderModel.Status loadStatus,
                                                          boolean active,
                                                          OrderBy orderBy,
                                                          int limit);

    Collection<Long> findIdByLoadStatusAndActive(DataLoaderModel.Status loadStatus,
                                                    boolean active,
                                                    OrderBy orderBy,
                                                    int limit);

    boolean existsByUuid(String dataLoaderUUID);
}
