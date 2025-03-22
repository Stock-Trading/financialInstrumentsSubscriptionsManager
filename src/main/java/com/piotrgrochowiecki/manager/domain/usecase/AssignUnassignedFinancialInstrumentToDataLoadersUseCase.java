package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.ports.FinancialInstrumentRepository;
import com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class AssignUnassignedFinancialInstrumentToDataLoadersUseCase {

    private final FinancialInstrumentService financialInstrumentService;
    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;

    @Transactional
    public void assignUnassignedInstrumentsToActiveDataLoaders() {
        if (!areThereAnyFinancialInstrumentsUnassignedToAnyDataLoader()) {
            log.debug("All financial instruments are assigned to data loaders.");
            return;
        }
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(getFinancialInstrumentsUnassignedToAnyDataLoader());
        List<DataLoaderModel> activeDataLoaders = new LinkedList<>(dataLoaderRepository.findActiveDataLoaders(
                DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
        );
        assignFIsToDLs(unassignedFIs, activeDataLoaders);
    }

    private void assignFIsToDLs(List<FinancialInstrumentModel> financialInstrumentModelList,
                                List<DataLoaderModel> dataLoaderModelList) {
        if (financialInstrumentModelList.isEmpty() || dataLoaderModelList.isEmpty()) {
            log.debug("Financial Instrument list or Data Loaders list is empty. Cannot assign FIs to DLs.");
            return;
        }
        int sizeOfDataLoaderList = dataLoaderModelList.size();
        int indexOfDLforFIassignement = 0;

        if (sizeOfDataLoaderList == 1) {
            log.debug("There is one available Data Loader (id={}, uuid={}). Assigning {} Financial Instruments" +
                            " into it.",
                    dataLoaderModelList.getFirst().getId(), dataLoaderModelList.getFirst().getUuid(),
                    financialInstrumentModelList.size());
            DataLoaderModel dataLoader = dataLoaderModelList.getFirst();
            financialInstrumentModelList.forEach(financialInstrumentModel -> {
                financialInstrumentModel.setDataLoaderId(dataLoader.getId());
                financialInstrumentService.update(financialInstrumentModel);
            });
            return;
        }
        while (!financialInstrumentModelList.isEmpty()) {
            FinancialInstrumentModel financialInstrumentModel = financialInstrumentModelList.getFirst();
            DataLoaderModel dataLoader = dataLoaderModelList.get(indexOfDLforFIassignement);
            financialInstrumentModel.setDataLoaderId(dataLoader.getId());
            log.debug("Assigned Data Loader with id {} to Financial Instrument {}",
                    dataLoader.getId(), financialInstrumentModel.getName());
            financialInstrumentService.update(financialInstrumentModel);
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

    //TODO do poprawy
    private Collection<FinancialInstrumentModel> getFinancialInstrumentsUnassignedToAnyDataLoader() {
        return financialInstrumentRepository.find5UnassignedToAnyDataLoader();
    }

}
