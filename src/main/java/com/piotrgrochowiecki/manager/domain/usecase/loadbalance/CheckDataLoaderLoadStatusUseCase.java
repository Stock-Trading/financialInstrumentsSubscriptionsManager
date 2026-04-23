package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

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
        log.info("Retrieving Data Loaders to check their load status");
        List<DataLoaderModel> dataLoaderModelList = dataLoaderRepository.findActiveDataLoaders(
                        DataLoaderRepository.OrderBy.LAST_LOAD_STATUS_UPDATE_ASC,
                        dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
                .stream()
                .toList();
        log.info("List of Data Loaders contains {} objects", dataLoaderModelList.size());
        dataLoaderModelList.forEach(this::checkAndUpdateLoadStatus);
    }

    private void checkAndUpdateLoadStatus(DataLoaderModel dataLoader) {
        log.info("Checking load status of dataLoader {}", dataLoader.toString());
        long numberOfAssignedFinancialInstruments = financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(
                dataLoader.getId());
        log.info("Number of assigned Financial Instruments: {}", numberOfAssignedFinancialInstruments);
        DataLoaderModel.Status loadStatus = getStatus(numberOfAssignedFinancialInstruments);
        dataLoader.setLoadStatus(loadStatus);
        dataLoader.setLastLoadStatusUpdate(timeService.getInstantUTC()); //updates time of last time
        // of Load Status flag update, so other instances of this service can retrieve records with "oldest"
        // lastLoadStatusUpdate
        log.debug("""
                        Data Loader with id {}, uuid {} has {} Financial Instruments assigned to it and its load status is {}.
                        Number of recommended Financial Instruments per Data Loader is {}.
                        """,
                dataLoader.getId(),
                dataLoader.getUuid(),
                numberOfAssignedFinancialInstruments,
                loadStatus,
                dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader());
        dataLoaderService.update(dataLoader);
    }

    private DataLoaderModel.Status getStatus(long numberOfAssignedFinancialInstruments) {
        DataLoaderModel.Status loadStatus;
        if (numberOfAssignedFinancialInstruments == dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
            loadStatus = DataLoaderModel.Status.BALANCED;
        } else if (numberOfAssignedFinancialInstruments < dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
            loadStatus = DataLoaderModel.Status.TOO_LOW;
        } else {
            loadStatus = DataLoaderModel.Status.TOO_HIGH;
        }
        return loadStatus;
    }

}
