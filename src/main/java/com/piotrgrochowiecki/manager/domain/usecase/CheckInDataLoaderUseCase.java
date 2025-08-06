package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
public class CheckInDataLoaderUseCase {

    private final DataLoaderService dataLoaderService;
    private final DataLoaderRepository dataLoaderRepository;
    private final TimeService timeService;

    @Transactional
    public DataLoaderModel checkIn(String dataLoaderUuid) {
        log.debug("Checking in data loader with uuid {}", dataLoaderUuid);
        DataLoaderModel dataLoaderModel = dataLoaderRepository.findByUuid(dataLoaderUuid);
        dataLoaderModel.setLastConnectedOn(timeService.getInstantUTC());
        dataLoaderModel.setActive(true);
        dataLoaderModel.setReadyForAssignmentOfFinancialInstruments(true);
        return dataLoaderService.update(dataLoaderModel);
    }

}
