package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import org.springframework.stereotype.Component;

@Component
class DataLoaderMapper {

    DataLoaderResponseDto mapToDto(DataLoaderModel model) {
        return DataLoaderResponseDto.builder()
                .uuid(model.getUuid())
                .checkedIn(model.getLastConnectedOn())
                .build();
    }

}
