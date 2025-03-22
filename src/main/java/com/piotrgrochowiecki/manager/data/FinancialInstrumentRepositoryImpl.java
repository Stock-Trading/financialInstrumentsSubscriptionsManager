package com.piotrgrochowiecki.manager.data;

import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.ports.FinancialInstrumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;

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
    public Collection<FinancialInstrumentModel> findUnassignedToAnyDataLoader(OrderBy orderBy, Integer limit) {
        Sort sort = mapper.mapToSort(orderBy);
        Pageable pageable = PageRequest.of(0, limit, sort);
        return jpaRepository.findByDataLoaderIsNull(pageable)
                .stream()
                .map(mapper::mapToFinancialInstrumentModel)
                .toList();
    }

    @Override
    public boolean existsWithNoDataLoaderAssigned() {
        return jpaRepository.existsByDataLoaderIdIsNull();
    }

}
