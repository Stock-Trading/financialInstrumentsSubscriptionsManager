package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import org.springframework.stereotype.Component;

@Component
class DataLoaderMapper {

    DataLoaderModel mapToModel(DataLoaderEntity entity) {
        return DataLoaderModel.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .lastConnected(entity.getLastConnected())
                .build();
    }

    DataLoaderEntity mapToEntity(DataLoaderModel model) {
        return DataLoaderEntity.builder()
                .uuid(model.uuid())
                .lastConnected(model.lastConnected())
                .build();
    }
}
