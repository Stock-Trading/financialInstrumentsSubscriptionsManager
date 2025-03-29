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
import java.util.LinkedList;
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
                        dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
                .stream()
                .toList();

        List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended = allDataLoaders.stream()
                .filter(this::doesHaveMoreFIsThanRecommended)
                .toList();

        List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended = allDataLoaders.stream()
                .filter(this::doesHaveLessFIsThanRecommended)
                .toList();

        int totalNumberOfFreeSpots = computeNumberOfFreeSpots(dataLoadersWithLessFIsThanRecommended);

        List<FinancialInstrumentModel> fIsToBeReassigned = getFIsToBeReassigned(dataLoadersWithMoreFIsThanRecommended,
                totalNumberOfFreeSpots);

        reassign(dataLoadersWithLessFIsThanRecommended, fIsToBeReassigned);
    }

    private boolean doesHaveMoreFIsThanRecommended(DataLoaderModel dataLoaderModel) {
        return dataLoaderModel.getFinancialInstrumentModelCollection()
                .size() > dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader();
    }


    private boolean doesHaveLessFIsThanRecommended(DataLoaderModel dataLoaderModel) {
        return dataLoaderModel.getFinancialInstrumentModelCollection()
                .size() < dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader();
    }

    private int computeNumberOfFreeSpots(List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended) {
        int totalNumberOfFreeSpots = 0;
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            int numberOfFIs = dataLoader.getFinancialInstrumentModelCollection()
                    .size();
            int freeSpots = Math.subtractExact(
                    dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader(),
                    numberOfFIs);
            totalNumberOfFreeSpots += freeSpots;
        }
        return totalNumberOfFreeSpots;
    }

    private List<FinancialInstrumentModel> getFIsToBeReassigned(List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended,
                                                                int totalNumberOfFreeSpots) {
        List<FinancialInstrumentModel> fIsToBeReassigned = new ArrayList<>();
        while (fIsToBeReassigned.size() < totalNumberOfFreeSpots) {
            boolean added = false;
            for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
                List<FinancialInstrumentModel> originalCollectionOfFIs = new LinkedList<>(dataLoader.getFinancialInstrumentModelCollection());
                List<FinancialInstrumentModel> excessiveFIs = originalCollectionOfFIs
                        .stream()
                        .skip(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
                        .toList();

                for (FinancialInstrumentModel fi : excessiveFIs) {
                    if (fIsToBeReassigned.size() < totalNumberOfFreeSpots) {
                        fIsToBeReassigned.add(fi);
                        added = true;
                        originalCollectionOfFIs.remove(fi);
                        dataLoader.setFinancialInstrumentModelCollection(originalCollectionOfFIs);
                    } else {
                        break;
                    }
                }
                if (!added) break;
                dataLoaderRepository.save(dataLoader);
            }
            if (!added) break;
        }
        return fIsToBeReassigned;
    }

    private void reassign(List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended,
                          List<FinancialInstrumentModel> fIsToBeReassigned) {
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            List<FinancialInstrumentModel> fIsOfGivenDataLoader = new ArrayList<>(dataLoader.getFinancialInstrumentModelCollection()
                    .stream()
                    .toList());

            while (fIsOfGivenDataLoader.size() < dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
                if (fIsToBeReassigned.isEmpty()) {
                    break;
                }
                fIsOfGivenDataLoader.add(fIsToBeReassigned.getFirst());
                fIsToBeReassigned.remove(0);
            }
            dataLoader.setFinancialInstrumentModelCollection(fIsOfGivenDataLoader);
            dataLoaderRepository.save(dataLoader);
        }
    }
}
