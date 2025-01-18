package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DataLoaderMapper {

    private final FinancialInstrumentMapper financialInstrumentMapper;

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
                        .map(financialInstrumentMapper::mapToFinancialInstrumentModel)
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
