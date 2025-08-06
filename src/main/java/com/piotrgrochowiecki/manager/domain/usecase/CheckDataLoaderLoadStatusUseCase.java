package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import com.piotrgrochowiecki.manager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class CheckDataLoaderLoadStatusUseCase {

    private final DataLoaderParametersProvider dataLoaderParametersProvider;
    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final DataLoaderService dataLoaderService;
    private final TimeService timeService;

    @Transactional
    public void checkLoadStatus() {
        List<DataLoaderModel> dataLoaderModelList = dataLoaderRepository.findActiveDataLoaders(
                        DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                        dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
                .stream()
                .toList();
        dataLoaderModelList.forEach(this::checkAndUpdateLoadStatus);
    }

    private void checkAndUpdateLoadStatus(DataLoaderModel dataLoader) {
        int numberOfFIsAssigned = financialInstrumentRepository.findByDataLoaderId(dataLoader
                        .getId())
                .size();
        DataLoaderModel.Status loadStatus;
        if (numberOfFIsAssigned == dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
            loadStatus = DataLoaderModel.Status.BALANCED;
        } else if (numberOfFIsAssigned < dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
            loadStatus = DataLoaderModel.Status.TOO_LOW;
        } else {
            loadStatus = DataLoaderModel.Status.TOO_HIGH;
        }
        dataLoader.setLoadStatus(loadStatus);
        dataLoader.setLastInstantOfFinancialInstrumentsAssignment(timeService.getInstantUTC()); //updates time of handling, so other instances of this
        // service can retrieve records with "oldest" lastInstantOfFinancialInstrumentsAssignment field in CheckReadyForHandlingStatus
        log.debug("""
                        Data Loader with id {} has {} Financial Instruments assigned to it and its load status is {}.
                        Number of recommended financial instruments per data loader is {}.
                        """,
                dataLoader.getId(),
                numberOfFIsAssigned,
                loadStatus,
                dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader());
        dataLoaderService.update(dataLoader);
    }
}
