package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DataMapper {

    FinancialInstrumentModel mapToFinancialInstrumentModel(FinancialInstrumentEntity entity) {
        return FinancialInstrumentModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .dataLoader(mapToDataLoaderModel(entity.getDataLoader()))
                .build();
    }

    FinancialInstrumentEntity mapToFinancialInstrumentEntity(FinancialInstrumentModel model) {
        return FinancialInstrumentEntity.builder()
                .name(model.name())
                .symbol(model.symbol())
                .dataLoader(mapToDataLoaderEntity(model.dataLoader()))
                .build();
    }

    DataLoaderModel mapToDataLoaderModel(DataLoaderEntity entity) {
        return DataLoaderModel.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .lastConnected(entity.getLastConnected())
                .financialInstrumentModelCollection(entity.getFinancialInstrument()
                        .stream()
                        .map(this::mapToFinancialInstrumentModel)
                        .collect(Collectors.toList())
                )
                .build();
    }

    DataLoaderEntity mapToDataLoaderEntity(DataLoaderModel model) {
        return DataLoaderEntity.builder()
                .uuid(model.uuid())
                .lastConnected(model.lastConnected())
                .financialInstrument(model.financialInstrumentModelCollection()
                        .stream()
                        .map(this::mapToFinancialInstrumentEntity)
                        .collect(Collectors.toList()))
                .build();
    }

}
