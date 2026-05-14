package com.piotrgrochowiecki.manager.data.financialinstrument;

import com.piotrgrochowiecki.manager.domain.component.FinancialInstrumentParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@AllArgsConstructor
@Log4j2
class FinancialInstrumentRepositoryImpl implements FinancialInstrumentRepository {

    private final FinancialInstrumentJpaRepository jpaRepository;
    private final FinancialInstrumentEntityMapper mapper;
    private final FinancialInstrumentParametersProvider parametersProvider;

    @Override
    public FinancialInstrumentModel save(FinancialInstrumentModel financialInstrumentModel) {
        FinancialInstrumentEntity entityToBeSaved = mapper.mapToFinancialInstrumentEntity(financialInstrumentModel);
        FinancialInstrumentEntity savedEntity = jpaRepository.save(entityToBeSaved);
        log.debug("Saved Financial Instrument entity {}", savedEntity);
        return mapper.mapToFinancialInstrumentModel(savedEntity);
    }

    @Override
    public Collection<FinancialInstrumentModel> findUnassignedToAnyDataLoader() {
        Sort sort = mapper.mapToSort(FinancialInstrumentRepository.OrderBy.CREATED_ON_ASC);
        int limit = parametersProvider.getRecommendedNumberOfFinancialInstrumentsUnassignedToAnyDataLoader();
        Pageable pageable = PageRequest.of(0, limit, sort);
        return jpaRepository.findByDataLoaderIdIsNull(pageable)
                .stream()
                .map(mapper::mapToFinancialInstrumentModel)
                .toList();
    }

    @Override
    public boolean existsWithNoDataLoaderAssigned() {
        return jpaRepository.existsByDataLoaderIdIsNull();
    }

    @Override
    public int detachDataLoaderBasedOnIds(List<Long> listOfFinancialInstrumentModelIds) {
        return jpaRepository.detachDataLoadersBasedOnIds(listOfFinancialInstrumentModelIds);
    }

    @Override
    public FinancialInstrumentModel attachDataLoaderById(FinancialInstrumentModel financialInstrumentModel, Long dataLoaderId) {
        financialInstrumentModel.setDataLoaderId(dataLoaderId);
        FinancialInstrumentEntity entityToBeSaved = mapper.mapToFinancialInstrumentEntity(financialInstrumentModel);
        FinancialInstrumentEntity savedEntity = jpaRepository.save(entityToBeSaved);
        return mapper.mapToFinancialInstrumentModel(savedEntity);
    }

    @Override
    public List<FinancialInstrumentModel> findByDataLoaderId(Long dataLoaderId) {
        return jpaRepository.findByDataLoaderId(dataLoaderId)
                .stream()
                .map(mapper::mapToFinancialInstrumentModel)
                .toList();
    }

    @Override
    public Long findNumberOfFinancialInstrumentsAssignedToDataLoader(Long dataLoaderId) {
        return jpaRepository.countByDataLoaderId(dataLoaderId);
    }
}
