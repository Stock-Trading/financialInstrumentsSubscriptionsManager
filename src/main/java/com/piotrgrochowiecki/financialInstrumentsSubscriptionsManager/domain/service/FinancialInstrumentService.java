package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;


import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.FinancialInstrumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
@Log4j2
public class FinancialInstrumentService {

    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final DataLoaderService dataLoaderService;

    @Scheduled(fixedDelay = 3000)
    private void assignUnassignedInstrumentsToActiveDataLoaders() {
        if (!areThereAnyFinancialInstrumentsUnassignedToAnyDataLoader()) {
            log.info("All financial instruments are assigned to data loaders.");
            return;
        }

        List<FinancialInstrumentModel> unassignedFIs = getFinancialInstrumentsUnassignedToAnyDataLoader().stream().toList();
        List<DataLoaderModel> activeDataLoadersWithNumberOfFIsLessThanEqualRecommendedOrderedAsc = dataLoaderService.getActiveDataLoadersWithNumberOfAssignedFinancialInstrumentsLessThanEqualRecommendedOrderAsc();
        List<DataLoaderModel> activeDataLoadersWithNumberOfFIsGreaterThanRecommendedOrderAsc = dataLoaderService.getActiveDataLoadersWithNumberOfAssignedFinancialInstrumentsGreaterThanRecommendedOrderAsc();

        if (activeDataLoadersWithNumberOfFIsLessThanEqualRecommendedOrderedAsc.isEmpty()) {
            assignFIstoDLs(unassignedFIs, activeDataLoadersWithNumberOfFIsGreaterThanRecommendedOrderAsc);
        } else {
            assignFIstoDLs(unassignedFIs, activeDataLoadersWithNumberOfFIsLessThanEqualRecommendedOrderedAsc);
        }
    }

    @Transactional
    public void assignFIstoDLs(List<FinancialInstrumentModel> financialInstrumentModels, List<DataLoaderModel> dataLoaderModels) {
        int sizeOfDataLoaderList = dataLoaderModels.size();
        int indexOfDLforFIassignement = 1;

        while (!financialInstrumentModels.isEmpty()) {
            FinancialInstrumentModel fItoBeAssigned = financialInstrumentModels.getFirst();
            DataLoaderModel dataLoader;
            if (indexOfDLforFIassignement <= sizeOfDataLoaderList) {
                dataLoader = dataLoaderModels.get(indexOfDLforFIassignement);
            } else {
                dataLoader = dataLoaderModels.get(indexOfDLforFIassignement - sizeOfDataLoaderList);
            }
            fItoBeAssigned.setDataLoader(dataLoader);
            financialInstrumentModels.removeFirst();
            indexOfDLforFIassignement++;
        }
    }

    public void unassignFromDataLoader(DataLoaderModel dataLoaderModel) {
        financialInstrumentRepository.unassignFromDataLoader(dataLoaderModel.getId());
        log.info("Unassigned all Financial Instruments from data loader with id {} and uuid {}", dataLoaderModel.getId(), dataLoaderModel.getUuid());
    }

    private boolean areThereAnyFinancialInstrumentsUnassignedToAnyDataLoader() {
        return financialInstrumentRepository.existsWithNoDataLoaderAssigned();
    }

    private Collection<FinancialInstrumentModel> getFinancialInstrumentsUnassignedToAnyDataLoader() {
        return financialInstrumentRepository.findAllUnassignedToAnyDataLoader();
    }

}

