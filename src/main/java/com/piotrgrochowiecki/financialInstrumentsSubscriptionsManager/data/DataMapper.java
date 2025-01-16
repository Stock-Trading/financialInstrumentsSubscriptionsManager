package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DataMapper {

    //TODO rozdzielić DataMapper pod każdą encję
    private final FinancialInstrumentJpaRepository financialInstrumentJpaRepository;

    FinancialInstrumentModel mapToFinancialInstrumentModel(FinancialInstrumentEntity entity) {
        return FinancialInstrumentModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .dataLoaderId(entity.getDataLoaderId())
                .build();
    }

    FinancialInstrumentEntity mapToFinancialInstrumentEntity(FinancialInstrumentModel model) {
        return FinancialInstrumentEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .symbol(model.getSymbol())
                .dataLoaderId(model.getDataLoaderId())
                .build();
    }

    DataLoaderModel mapToDataLoaderModel(DataLoaderEntity entity) {
        if (Objects.isNull(entity.getFinancialInstrument())) {
            return DataLoaderModel.builder()
                    .id(entity.getId())
                    .uuid(entity.getUuid())
                    .lastConnectedOn(entity.getLastConnectedOn())
                    .active(entity.getActive())
                    .build();
        }
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
