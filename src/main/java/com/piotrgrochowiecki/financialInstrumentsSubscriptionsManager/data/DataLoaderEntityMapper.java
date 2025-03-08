package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DataLoaderEntityMapper {

    private final FinancialInstrumentEntityMapper financialInstrumentEntityMapper;

    DataLoaderModel mapToDataLoaderModel(DataLoaderEntity entity) {
        DataLoaderModel model = DataLoaderModel.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .lastConnectedOn(entity.getLastConnectedOn())
                .lastHandledOn(entity.getLastHandledOn())
                .readyForHandling(entity.getReadyForHandling())
                .loadStatus(
                        DataLoaderModel.Status
                                .getStatusByDbValue(
                                        entity.getLoadStatus()
                                )
                )
                .active(entity.getActive())
                .build();

        if (!Objects.isNull(entity.getFinancialInstrument())) {
            model.setFinancialInstrumentModelCollection(entity.getFinancialInstrument()
                    .stream()
                    .map(financialInstrumentEntityMapper::mapToFinancialInstrumentModel)
                    .collect(Collectors.toList())
            );
        }
        return model;
    }

    DataLoaderEntity mapToDataLoaderEntity(DataLoaderModel model) {
        return DataLoaderEntity.builder()
                .id(model.getId())
                .uuid(model.getUuid())
                .lastConnectedOn(model.getLastConnectedOn())
                .lastHandledOn(model.getLastHandledOn())
                .active(model.getActive())
                .readyForHandling(model.getReadyForHandling())
                .loadStatus(model.getLoadStatus()
                        .getDbValue())
                .build();
    }

        DataLoaderModel.DataLoaderLoadStatus modelLoadStatus = model.getLoadStatus();
        if (!Objects.isNull(model.getLoadStatus())) {
            switch (modelLoadStatus) {
                case TOO_LOW -> entity.setLoadStatus(DataLoaderEntity.DataLoaderLoadStatus.TOO_LOW);
                case TOO_HIGH -> entity.setLoadStatus(DataLoaderEntity.DataLoaderLoadStatus.TOO_HIGH);
                case BALANCED -> entity.setLoadStatus(DataLoaderEntity.DataLoaderLoadStatus.BALANCED);
            }
        }
        return entity;
    }

}
