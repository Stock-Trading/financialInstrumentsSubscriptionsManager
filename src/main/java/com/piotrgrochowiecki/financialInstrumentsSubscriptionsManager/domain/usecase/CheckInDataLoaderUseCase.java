package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.usecase;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.DataLoaderRepository;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.TimeService;
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
        DataLoaderModel dataLoaderModel = dataLoaderRepository.findByUuid(dataLoaderUuid);
        dataLoaderModel.setLastConnectedOn(timeService.getInstantUTC());
        dataLoaderModel.setActive(true);
        dataLoaderModel.setReadyForHandling(true);
        return dataLoaderService.update(dataLoaderModel);
    }

}
