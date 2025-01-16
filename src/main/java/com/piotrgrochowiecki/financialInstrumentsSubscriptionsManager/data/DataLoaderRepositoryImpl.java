package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.DataLoaderRepository;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.TimeService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    private final DataMapper mapper;
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
    @Transactional
    public void updateDataLoaderLastConnectedOnTime(String dataLoaderUuid, Instant lastConnectedOn) {
        jpaRepository.updateDataLoaderLastConnectedOnTime(dataLoaderUuid, lastConnectedOn);
        log.info("Updated data loader with uuid {}. Last connected on {}", dataLoaderUuid, LocalDateTime.ofInstant(lastConnectedOn, ZoneOffset.UTC));
    }

    @Override
    public DataLoaderModel findByUuid(String dataLoaderUUID) {
        Optional<DataLoaderEntity> dataLoaderEntityOptional = jpaRepository.findByUuid(dataLoaderUUID);
        if (dataLoaderEntityOptional.isPresent()) {
            return mapper.mapToDataLoaderModel(dataLoaderEntityOptional.get());
        }
        throw new EntityNotFoundException("No DataLoaderEntity found with uuid " + dataLoaderUUID);
    }

    @Override
    public List<DataLoaderModel> find5ActiveDataLoaders() {
        return jpaRepository.findTop5ByActiveTrue().stream()
                .map(mapper::mapToDataLoaderModel)
                .toList();
    }

    @Override
    public Collection<DataLoaderModel> find5InactiveDataLoaders(Duration timeFromLastConnectionAsHealthThreshold) {
        Instant lastInstantCountingAsHealthy = timeService.getInstantUTC().minus(timeFromLastConnectionAsHealthThreshold);
        return jpaRepository.findByLastConnectedOnLessThanEqualLimit5(lastInstantCountingAsHealthy).stream()
                .map(mapper::mapToDataLoaderModel)
                .toList();
    }

    @Override
    public Collection<DataLoaderModel> findAllActiveAndUnhandledDataLoaders(Duration timeFromLastConnectionAsHealthThreshold, Duration timeFromLastHandledTimeAsUnhandledThreshold) {
        Instant lastInstantCountingAsHealthy = timeService.getInstantUTC().minus(timeFromLastConnectionAsHealthThreshold);
        Instant firstInstantCountingAsUnhandled = timeService.getInstantUTC().minus(timeFromLastHandledTimeAsUnhandledThreshold);
        return jpaRepository.findByLastConnectedOnGreaterThanAndLastHandledOnLessThanEqual(lastInstantCountingAsHealthy, firstInstantCountingAsUnhandled).stream()
                .map(mapper::mapToDataLoaderModel)
                .toList();
    }

    @Override
    public List<DataLoaderModel> findAllWithNumberOfAssignedFinancialInstrumentsGreaterThanRecommendedAndLastConnectedOnGreaterThanOrderAscByFinancialInstrumentCount(Integer recommendedNumberOfFinancialInstrumentsPerDataLoader, Duration timeFromLastConnectionAsHealthThreshold) {
        Instant lastInstantCountingAsHealthy = timeService.getInstantUTC().minus(timeFromLastConnectionAsHealthThreshold);
        return jpaRepository.findAllWithNumberOfAssignedFinancialInstrumentsGreaterThanRecommendedOrderAsc(recommendedNumberOfFinancialInstrumentsPerDataLoader).stream()
                .map(mapper::mapToDataLoaderModel)
                .toList();
    }

    @Override
    public List<DataLoaderModel> findAllWithNumberOfAssignedFinancialInstrumentsLessThanEqualRecommendedAndLastConnectedOnGreaterThanOrderAscByFinancialInstrumentCount(Integer recommendedNumberOfFinancialInstrumentsPerDataLoader, Duration timeFromLastConnectionAsHealthThreshold) {
        Instant lastInstantCountingAsHealthy = timeService.getInstantUTC().minus(timeFromLastConnectionAsHealthThreshold);
        return jpaRepository.findAllWithNumberOfAssignedFinancialInstrumentsLessThanEqualRecommendedAndLastConnectedOnGreaterThanOrderAscByFinancialInstrumentCount(recommendedNumberOfFinancialInstrumentsPerDataLoader, lastInstantCountingAsHealthy).stream()
                .map(mapper::mapToDataLoaderModel)
                .toList();
    }

    @Override
    public boolean existsByUuid(String dataLoaderUUUID) {
        return jpaRepository.existsByUuid(dataLoaderUUUID);
    }
}
