package com.piotrgrochowiecki.manager.data;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
                    .toList()
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

    Sort mapToSort(DataLoaderRepository.OrderBy orderBy) {
        switch (orderBy) {
            case DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC -> {
                return Sort.sort(DataLoaderEntity.class)
                        .by(DataLoaderEntity::getLastConnectedOn)
                        .ascending();
            }
            case DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_DESC -> {
                return Sort.sort(DataLoaderEntity.class)
                        .by(DataLoaderEntity::getLastConnectedOn)
                        .descending();
            }
            default -> throw new IllegalArgumentException("Unknown enum " + orderBy.name());
        }
    }
}
