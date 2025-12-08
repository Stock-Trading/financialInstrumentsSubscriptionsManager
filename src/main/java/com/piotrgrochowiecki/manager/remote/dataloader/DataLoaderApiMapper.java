package com.piotrgrochowiecki.manager.remote.dataloader;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import org.springframework.stereotype.Component;

@Component
class DataLoaderApiMapper {

    DataLoaderResponseDto mapToDto(DataLoaderModel model) {
        return DataLoaderResponseDto.builder()
                .uuid(model.getUuid())
                .checkedIn(model.getLastConnectedOn())
                .build();
    }

}
