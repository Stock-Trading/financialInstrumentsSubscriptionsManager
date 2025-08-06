package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

@Log4j2
@Component
@RequiredArgsConstructor
public class CheckForReadinessOfDataLoaderForAssignmentOfFinancialInstrumentsUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final DataLoaderService dataLoaderService;
    private final TimeService timeService;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;

    @Transactional
    public void checkIfDataLoadersAreReadyForAssignmentOfFinancialInstruments() {
        Collection<DataLoaderModel> dataLoaders = dataLoaderRepository.findReadyForAssignmentOfFinancialInstruments(
                DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC, //retrieves data loaders based on lastInstantOfFinancialInstrumentsAssignment field
                // starting with the oldest one (ASC)
                dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()
        );
        dataLoaders.forEach(this::checkReadyForHandlingStatusAndUpdate);
    }

    private void checkReadyForHandlingStatusAndUpdate(DataLoaderModel dataLoader) {
        Instant lastInstantCountingAsReadyForHandling = timeService.getInstantUTC()
                .minus(Duration.ofMillis(
                                dataLoaderParametersProvider.getReadyForHandlingThresholdMilliseconds()));
        if (dataLoader.getLastConnectedOn()
                .isBefore(lastInstantCountingAsReadyForHandling)) {
            log.info("""
                            Data Loader id={}, uuid={} last connected on {}, which is before last point in time
                            counting as Ready for Handling. Setting its readyForHandling flag to FALSE.
                            Threshold of ready for handling is set to {} milliseconds
                            """,
                    dataLoader.getId(),
                    dataLoader.getUuid(),
                    dataLoader.getLastConnectedOn(),
                    dataLoaderParametersProvider.getReadyForHandlingThresholdMilliseconds()
            );
            dataLoader.setReadyForAssignmentOfFinancialInstruments(false);
            dataLoaderService.update(dataLoader);
        }
    }

}
