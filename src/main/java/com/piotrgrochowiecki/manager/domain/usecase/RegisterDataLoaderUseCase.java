package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.exception.ModelAlreadyExistsException;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
public class RegisterDataLoaderUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final TimeService timeService;

    @Transactional
    public DataLoaderModel register(String dataLoaderUuid) {
        if (dataLoaderRepository.existsByUuid(dataLoaderUuid)) {
            log.error("Data loader with uuid {} has already been registered", dataLoaderUuid);
            throw new ModelAlreadyExistsException("Data loader with uuid " +
                    dataLoaderUuid + " has already been registered");
        }
        DataLoaderModel dataLoaderModel = DataLoaderModel.builder()
                .uuid(dataLoaderUuid)
                .lastConnectedOn(timeService.getInstantUTC())
                .active(true)
                .loadStatus(DataLoaderModel.Status.TOO_LOW)
                .build();
        return dataLoaderRepository.save(dataLoaderModel);
    }

}
