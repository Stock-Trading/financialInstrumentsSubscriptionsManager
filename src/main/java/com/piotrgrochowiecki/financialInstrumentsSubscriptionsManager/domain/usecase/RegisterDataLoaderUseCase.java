package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.usecase;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception.ModelAlreadyExistsException;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.DataLoaderRepository;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.TimeService;
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
                .build();
        return dataLoaderRepository.save(dataLoaderModel);
    }
}
