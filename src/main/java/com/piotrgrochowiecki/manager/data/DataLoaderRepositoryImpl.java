package com.piotrgrochowiecki.manager.data;

import com.piotrgrochowiecki.manager.domain.exception.NotFoundException;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Log4j2
public class DataLoaderRepositoryImpl implements DataLoaderRepository {

    private final DataLoaderJpaRepository jpaRepository;
    private final DataLoaderEntityMapper mapper;
    private final TimeService timeService;

    @Override
    @Transactional
    public DataLoaderModel save(DataLoaderModel dataLoaderModel) {
        DataLoaderEntity entityToBeSaved = mapper.mapToDataLoaderEntity(dataLoaderModel);
        DataLoaderEntity savedEntity = jpaRepository.save(entityToBeSaved);
        log.info("Saved data loader entity {}", savedEntity);
        return mapper.mapToDataLoaderModel(savedEntity);
    }

    @Override
    public DataLoaderModel findByUuid(String dataLoaderUUID) {
        Optional<DataLoaderEntity> dataLoaderEntityOptional = jpaRepository.findByUuid(dataLoaderUUID);
        return mapper.mapToDataLoaderModel(dataLoaderEntityOptional.orElseThrow(
                () -> new NotFoundException("No DataLoaderEntity found with uuid " + dataLoaderUUID))
        );
    }

    @Override
    public List<DataLoaderModel> findActiveDataLoaders(OrderBy orderBy, int limit) {
        Sort sort = mapper.mapToSort(orderBy);
        Pageable pageable = PageRequest.of(0, limit, sort);
        return jpaRepository.findAllActive(pageable)
                .stream()
                .map(mapper::mapToDataLoaderModel)
                .toList();
    }

    /**
     * Sets <i>Active</i> flag to <b>false</b> and <i>LoadStatus</i> to <b>null</b> of DataLoaders that are active (=true),
     * but they have not connected to the service in specified threshold.
     *
     * @param lastConnectedOn Instant of last time when Data Loader connected to the service
     * @return number of updated rows
     */
    @Override
    public Integer setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(Instant lastConnectedOn) {
        return jpaRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(lastConnectedOn);
    }

    @Override
    public Collection<DataLoaderModel> findBasedOnLoadStatusAndActive(DataLoaderModel.Status loadStatus,
                                                                      boolean active,
                                                                      OrderBy orderBy,
                                                                      int limit) {
        Sort sort = mapper.mapToSort(OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC);
        Pageable pageable = PageRequest.of(0, limit, sort);
        String loadStatusStr = loadStatus.getDbValue();
        return jpaRepository.findByLoadStatusAndActive(loadStatusStr,
                        active,
                        pageable)
                .stream()
                .map(mapper::mapToDataLoaderModel)
                .toList();
    }

    @Override
    public boolean existsByUuid(String dataLoaderUUUID) {
        return jpaRepository.existsByUuid(dataLoaderUUUID);
    }
}
