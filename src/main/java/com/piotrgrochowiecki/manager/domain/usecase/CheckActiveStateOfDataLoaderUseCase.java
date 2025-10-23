package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Log4j2
@Component
@RequiredArgsConstructor
public class CheckActiveStateOfDataLoaderUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;
    private final TimeService timeService;

    /**
     * Checks for last check-in time of DataLoader. If that time is longer then specified threshold,
     * unassigns all Financial Instrument from it and sets its Active and ReadyForFinancialInstrumentsAssignment
     * properties to false.
     */
    @Transactional
    public void checkActiveState() {
        Instant lastInstantCountingAsActive = timeService.getInstantUTC()
                .minus(Duration.ofMillis(dataLoaderParametersProvider.getActiveThresholdMilliseconds()));

        Integer numberOfUpdatedModels = dataLoaderRepository.checkForInactiveDataLoadersAndUpdateTheirProperties(
                lastInstantCountingAsActive);

        log.debug("Number of updated data loaders: {}", numberOfUpdatedModels);
    }
}
