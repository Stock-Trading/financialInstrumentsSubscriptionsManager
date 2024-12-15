package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.FinancialInstrumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class FinancialInstrumentRepositoryImpl implements FinancialInstrumentRepository {

    private final FinancialInstrumentJpaRepository jpaRepository;
    private final DataMapper mapper;

    @Override
    public Collection<FinancialInstrumentModel> findAllUnassignedToAnyDataLoader() {
        return jpaRepository.findUnassignedToAnyDataLoader().stream()
                .map(mapper::mapToFinancialInstrumentModel)
                .toList();
    }

    @Override
    public boolean existsWithNoDataLoaderAssigned() {
        return jpaRepository.existsWithNoDataLoaderAssigned();
    }

    @Override
    public void unassignFromDataLoader(Long dataLoaderId) {
        jpaRepository.unassignFromDataLoader(dataLoaderId);
    }

}
