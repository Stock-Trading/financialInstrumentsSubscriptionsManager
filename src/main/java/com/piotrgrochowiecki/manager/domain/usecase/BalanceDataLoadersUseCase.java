package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class BalanceDataLoadersUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;

    @Transactional
    public void balanceDataLoaders() {
        List<DataLoaderModel> allDataLoaders = dataLoaderRepository.findActiveAndUnhandledDataLoaders(
                        Duration.ofMillis(dataLoaderParametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()),
                        Duration.ofMillis(dataLoaderParametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()),
                        DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                        dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()
                )
                .stream()
                .toList();

        List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended = allDataLoaders.stream()
                .filter(dataLoaderModel ->
                        dataLoaderModel.getFinancialInstrumentModelCollection()
                                .size()
                                > dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()
                )
                .toList();

        List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended = allDataLoaders.stream()
                .filter(dataLoaderModel ->
                        dataLoaderModel.getFinancialInstrumentModelCollection()
                                .size()
                                <= dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()
                )
                .toList();

        int totalNumberOfFreeSpots = 0;
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            int numberOfFIs = dataLoader.getFinancialInstrumentModelCollection()
                    .size();
            int freeSpots = Math.subtractExact(
                    dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader(),
                    numberOfFIs
            );
            totalNumberOfFreeSpots += freeSpots;
        }

        List<FinancialInstrumentModel> fIsToBeReassigned = new ArrayList<>();
        while (fIsToBeReassigned.size() <= totalNumberOfFreeSpots) {
            for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
                List<FinancialInstrumentModel> temporaryListOfFIs = dataLoader.getFinancialInstrumentModelCollection()
                        .stream()
                        .toList();
                for (int i = dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader(); i < temporaryListOfFIs.size() - 1; i++) {
                    fIsToBeReassigned.add(temporaryListOfFIs.get(i));
                }
            }
        }

        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            List<FinancialInstrumentModel> fIsOfGivenDataLoader = new ArrayList<>(dataLoader.getFinancialInstrumentModelCollection()
                    .stream()
                    .toList());
            while (fIsOfGivenDataLoader.size() <= dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
                fIsOfGivenDataLoader.add(fIsToBeReassigned.getFirst());
            }
            dataLoader.setFinancialInstrumentModelCollection(fIsOfGivenDataLoader);
        }
    }
}
