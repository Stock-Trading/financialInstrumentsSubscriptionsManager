package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.usecase;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.DataLoaderRepository;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.TimeService;
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
public class CheckActiveStateOfDataLoaderUseCase {

    private final DataLoaderService dataLoaderService;
    private final DataLoaderRepository dataLoaderRepository;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;
    private final TimeService timeService;

    /**
     * Checks for last check-in time of DataLoader. If that time is longer then specified threshold,
     * unassigns FinancialInstrument from it and sets its Active property to false.
     */
    @Transactional
    public void checkActiveState() {
        Collection<DataLoaderModel> dataLoaders = dataLoaderRepository.findActiveDataLoaders(
                DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()
        );
        dataLoaders.forEach(this::checkIfQualifiesAsInactiveAndUpdate);
    }

    private void checkIfQualifiesAsInactiveAndUpdate(DataLoaderModel dataLoader) {
        Instant lastInstantCountingAsHealthy = timeService.getInstantUTC()
                .minus(Duration.ofMillis(
                                dataLoaderParametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()
                        )
                );
        if (checkIfQualifiesAsUnhealthy(dataLoader, lastInstantCountingAsHealthy)) {
            setConditionsOfUnhealthyAndUpdate(dataLoader);
        }
    }

    private boolean checkIfQualifiesAsUnhealthy(DataLoaderModel dataLoader, Instant lastInstantCountingAsHealthy) {
        if (dataLoader.getLastConnectedOn()
                .isBefore(lastInstantCountingAsHealthy)) {
            log.info("""
                            Data Loader id={}, uuid={} last connected on {}, which is before last point in time counting
                            as healthy. Setting its loadStatus and Financial Instrument Collection to null and
                            Active to false. Threshold of healthy is set to {} milliseconds.
                            """,
                    dataLoader.getId(),
                    dataLoader.getUuid(),
                    dataLoader.getLastConnectedOn(),
                    dataLoaderParametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()
            );
            return true;
        }
        return false;
    }

    private void setConditionsOfUnhealthyAndUpdate(DataLoaderModel dataLoader) {
        dataLoader.setFinancialInstrumentModelCollection(null);
        dataLoader.setLoadStatus(null);
        dataLoader.setActive(false);
        dataLoader.setReadyForHandling(false);
        dataLoaderService.update(dataLoader);
    }

}
