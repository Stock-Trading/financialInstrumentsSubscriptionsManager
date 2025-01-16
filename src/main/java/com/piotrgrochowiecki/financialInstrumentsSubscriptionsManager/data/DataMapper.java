package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DataMapper {

    private final FinancialInstrumentJpaRepository financialInstrumentJpaRepository;

    FinancialInstrumentModel mapToFinancialInstrumentModel(FinancialInstrumentEntity entity) {
        DataLoaderEntity dataLoaderEntity = entity.getDataLoader();
        if (Objects.isNull(dataLoaderEntity)) {
            return FinancialInstrumentModel.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .symbol(entity.getSymbol())
                    .build();
        }
        return FinancialInstrumentModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .dataLoader(mapToDataLoaderModel(dataLoaderEntity))
                .build();
    }

    FinancialInstrumentEntity mapToFinancialInstrumentEntity(FinancialInstrumentModel model) {
        if (Objects.isNull(model.getDataLoader())) {
            return FinancialInstrumentEntity.builder()
                    .id(model.getId())
                    .name(model.getName())
                    .symbol(model.getSymbol())
                    .build();
        }
        return FinancialInstrumentEntity.builder()
                .name(model.getName())
                .symbol(model.getSymbol())
                .dataLoader(mapToDataLoaderEntity(model.getDataLoader()))
                .build();
    }

    DataLoaderModel mapToDataLoaderModel(DataLoaderEntity entity) {
        Collection<FinancialInstrumentEntity> financialInstrumentEntityCollection = financialInstrumentJpaRepository.findByDataLoaderId(entity.getId());
        return DataLoaderModel.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .lastConnectedOn(entity.getLastConnectedOn())
                .active(entity.getActive())
                .financialInstrumentModelCollection(entity.getFinancialInstrument().stream()
                        .map(this::mapToFinancialInstrumentModel)
                        .collect(Collectors.toList())
                )
                .build();
    }

    DataLoaderEntity mapToDataLoaderEntity(DataLoaderModel model) {
        return DataLoaderEntity.builder()
                .id(model.getId())
                .uuid(model.getUuid())
                .lastConnectedOn(model.getLastConnectedOn())
                .active(model.getActive())
                .build();
    }

}
