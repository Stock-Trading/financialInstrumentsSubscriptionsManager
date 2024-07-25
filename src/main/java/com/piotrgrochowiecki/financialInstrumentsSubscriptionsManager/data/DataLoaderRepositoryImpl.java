package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DataLoaderRepositoryImpl implements DataLoaderRepository {

    private final DataLoaderJpaRepository jpaRepository;

    @Override
    public DataLoaderModel save(DataLoaderModel dataLoaderModel) {
        return null;
    }

    @Override
    public Collection<DataLoaderModel> findAll() {
        return List.of();
    }

    @Override
    public Optional<DataLoaderModel> findByUuid(String dataLoaderUUID) {
        return Optional.empty();
    }

    @Override
    public Collection<FinancialInstrumentModel> findAllFinancialInstrumentsByDataLoaderUuid(String dataLoaderUUID) {
        return List.of();
    }

    @Override
    public boolean existsByUuid(String dataLoaderUUUID) {
        return jpaRepository.existsByUuid(dataLoaderUUUID);
    }
}
