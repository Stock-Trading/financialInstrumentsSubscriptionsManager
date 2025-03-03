package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.FinancialInstrumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    public Collection<FinancialInstrumentModel> find5UnassignedToAnyDataLoader() {
        return jpaRepository.findTop5ByDataLoaderIsNull().stream()
                .map(mapper::mapToFinancialInstrumentModel)
                .toList();
    }

    @Override
    public boolean existsWithNoDataLoaderAssigned() {
        return jpaRepository.existsByDataLoaderIdIsNull();
    }

}
