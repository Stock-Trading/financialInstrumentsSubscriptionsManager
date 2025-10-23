package com.piotrgrochowiecki.manager.data;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class DataLoaderEntityMapper {

    DataLoaderModel mapToDataLoaderModel(DataLoaderEntity entity) {
        return DataLoaderModel.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .lastConnectedOn(entity.getLastConnectedOn())
                .lastInstantOfFinancialInstrumentsAssignment(entity.getLastInstantOfFinancialInstrumentsAssignment())
                .loadStatus(Optional.ofNullable(entity.getLoadStatus())
                        .map(DataLoaderModel.Status::getStatusByDbValue)
                        .orElse(null))
                .active(entity.getActive())
                .build();
    }

    DataLoaderEntity mapToDataLoaderEntity(DataLoaderModel model) {
        return DataLoaderEntity.builder()
                .id(model.getId())
                .uuid(model.getUuid())
                .lastConnectedOn(model.getLastConnectedOn())
                .lastInstantOfFinancialInstrumentsAssignment(model.getLastInstantOfFinancialInstrumentsAssignment())
                .active(model.getActive())
                .loadStatus(Optional.ofNullable(model.getLoadStatus())
                        .map(DataLoaderModel.Status::getDbValue)
                        .orElse(null))
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
            case DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC -> {
                return Sort.sort(DataLoaderEntity.class)
                        .by(DataLoaderEntity::getLastInstantOfFinancialInstrumentsAssignment)
                        .ascending();
            }
            case DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_DESC -> {
                return Sort.sort(DataLoaderEntity.class)
                        .by(DataLoaderEntity::getLastInstantOfFinancialInstrumentsAssignment)
                        .descending();
            }
            default -> throw new IllegalArgumentException("Unknown enum " + orderBy.name());
        }
    }
}
