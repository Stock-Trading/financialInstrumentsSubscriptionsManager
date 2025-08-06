package com.piotrgrochowiecki.manager.data;

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

    @Override
    public FinancialInstrumentModel save(FinancialInstrumentModel financialInstrumentModel) {
        FinancialInstrumentEntity entityToBeSaved = mapper.mapToFinancialInstrumentEntity(financialInstrumentModel);
        FinancialInstrumentEntity savedEntity = jpaRepository.save(entityToBeSaved);
        log.debug("Saved Financial Instrument entity {}", savedEntity);
        return mapper.mapToFinancialInstrumentModel(savedEntity);
    }

    @Override
    public Collection<FinancialInstrumentModel> findUnassignedToAnyDataLoader(OrderBy orderBy, int limit) {
        Sort sort = mapper.mapToSort(orderBy);
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
    public FinancialInstrumentModel detachDataLoader(FinancialInstrumentModel financialInstrumentModel) {
        financialInstrumentModel.setDataLoaderId(null);
        FinancialInstrumentEntity entityToBeSaved = mapper.mapToFinancialInstrumentEntity(financialInstrumentModel);
        FinancialInstrumentEntity savedEntity = jpaRepository.save(entityToBeSaved);
        return mapper.mapToFinancialInstrumentModel(savedEntity);
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
}
