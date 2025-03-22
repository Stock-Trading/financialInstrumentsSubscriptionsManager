package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
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
public class CheckReadyForHandlingStatusOfDataLoaderUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final DataLoaderService dataLoaderService;
    private final TimeService timeService;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;

    @Transactional
    public void checkReadyForHandlingStatus() {
        Collection<DataLoaderModel> dataLoaders = dataLoaderRepository.findReadyForHandling(
                DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()
        );
        dataLoaders.forEach(this::checkReadyForHandlingStatusAndUpdate);
    }

    private void checkReadyForHandlingStatusAndUpdate(DataLoaderModel dataLoader) {
        Instant lastInstantCountingAsReadyForHandling = timeService.getInstantUTC()
                .minus(
                        Duration.ofMillis(
                                dataLoaderParametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()
                        )
                );
        if (dataLoader.getLastConnectedOn()
                .isBefore(lastInstantCountingAsReadyForHandling)) {
            log.info("""
                            Data Loader id={}, uuid={} last connected on {}, which is before last point in time
                            counting as Ready for Handling. Setting its readyForHandling field to FALSE.
                            Threshold of ready for handling is set to {} milliseconds
                            """,
                    dataLoader.getId(),
                    dataLoader.getUuid(),
                    dataLoader.getLastConnectedOn(),
                    dataLoaderParametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()
            );
            dataLoader.setReadyForHandling(false);
            dataLoaderService.update(dataLoader);
        }
    }

}
