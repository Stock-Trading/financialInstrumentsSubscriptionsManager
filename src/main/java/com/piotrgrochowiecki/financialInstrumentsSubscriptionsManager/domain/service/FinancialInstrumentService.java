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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Log4j2
public class FinancialInstrumentService {

    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final DataLoaderService dataLoaderService;

    @Transactional
    public void update(FinancialInstrumentModel financialInstrumentModel) {
        if (Objects.isNull(financialInstrumentModel.getId())) {
            throw new RuntimeException("Cannot update Financial Instrument as its id is null");
        }
        financialInstrumentRepository.save(financialInstrumentModel);
    }

    @Scheduled(fixedDelay = 10_000)
    @Transactional
    void assignUnassignedInstrumentsToActiveDataLoaders() {
        log.debug("Starting regular task of assigning unassigned Financial Instruments to active Data Loaders");
        if (!areThereAnyFinancialInstrumentsUnassignedToAnyDataLoader()) {
            log.debug("All financial instruments are assigned to data loaders.");
            return;
        }
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(getFinancialInstrumentsUnassignedToAnyDataLoader());
        List<DataLoaderModel> activeDataLoaders = new LinkedList<>(dataLoaderService.getActiveDataLoaders());
        assignFIsToDLs(unassignedFIs, activeDataLoaders);
    }

    private void assignFIsToDLs(List<FinancialInstrumentModel> financialInstrumentModelList, List<DataLoaderModel> dataLoaderModelList) {
        if (financialInstrumentModelList.isEmpty() || dataLoaderModelList.isEmpty()) {
            log.debug("Financial Instrument list or Data Loaders list is empty. Cannot assign FIs to DLs.");
            return;
        }
        int sizeOfDataLoaderList = dataLoaderModelList.size();
        int indexOfDLforFIassignement = 0;

        if (sizeOfDataLoaderList == 1) {
            log.debug("There is one available Data Loader (id={}, uuid={}). Assigning {} Financial Instruments into it.",
                    dataLoaderModelList.getFirst().getId(), dataLoaderModelList.getFirst().getUuid(), financialInstrumentModelList.size());
            DataLoaderModel dataLoader = dataLoaderModelList.getFirst();
            financialInstrumentModelList.forEach(financialInstrumentModel -> {
                financialInstrumentModel.setDataLoaderId(dataLoader.getId());
                update(financialInstrumentModel);
            });
            return;
        }
        while (!financialInstrumentModelList.isEmpty()) {
            FinancialInstrumentModel financialInstrumentModel = financialInstrumentModelList.getFirst();
            DataLoaderModel dataLoader = dataLoaderModelList.get(indexOfDLforFIassignement);
            financialInstrumentModel.setDataLoaderId(dataLoader.getId());
            log.debug("Assigned Data Loader with id {} to Financial Instrument {}",
                    dataLoader.getId(), financialInstrumentModel.getName());
            update(financialInstrumentModel);
            financialInstrumentModelList.removeFirst();
            indexOfDLforFIassignement++;
            if (indexOfDLforFIassignement == sizeOfDataLoaderList - 1) {
                indexOfDLforFIassignement = 0;
                log.info("Starting another round of reassigning Financial Instruments to Data Loaders.");
            }
        }
    }

    private boolean areThereAnyFinancialInstrumentsUnassignedToAnyDataLoader() {
        return financialInstrumentRepository.existsWithNoDataLoaderAssigned();
    }

    private Collection<FinancialInstrumentModel> getFinancialInstrumentsUnassignedToAnyDataLoader() {
        return financialInstrumentRepository.find5UnassignedToAnyDataLoader();
    }

}

