package com.piotrgrochowiecki.manager.data;

import com.piotrgrochowiecki.manager.domain.exception.NotFoundException;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
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

    public Collection<DataLoaderModel> findReadyForHandling(OrderBy orderBy, int limit) {
        Sort sort = mapper.mapToSort(orderBy);
        Pageable pageable = PageRequest.of(0, limit, sort);
        return jpaRepository.findReadyForHandling(pageable)
                .stream()
                .map(mapper::mapToDataLoaderModel)
                .toList();
    }

    @Override
    public Collection<DataLoaderModel> findActiveAndUnhandledDataLoaders(Duration timeFromLastConnectionAsHealthThreshold,
                                                                         Duration timeFromLastHandledTimeAsUnhandledThreshold,
                                                                         OrderBy orderBy,
                                                                         int limit) {
        Instant lastInstantCountingAsHealthy = timeService.getInstantUTC()
                .minus(timeFromLastConnectionAsHealthThreshold);
        Instant firstInstantCountingAsUnhandled = timeService.getInstantUTC()
                .minus(timeFromLastHandledTimeAsUnhandledThreshold);
        Sort sort = mapper.mapToSort(orderBy);
        Pageable pageable = PageRequest.of(0, limit, sort);
        return jpaRepository.findByLastConnectedOnGreaterThanAndLastHandledOnLessThanEqual(lastInstantCountingAsHealthy,
                        firstInstantCountingAsUnhandled,
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
