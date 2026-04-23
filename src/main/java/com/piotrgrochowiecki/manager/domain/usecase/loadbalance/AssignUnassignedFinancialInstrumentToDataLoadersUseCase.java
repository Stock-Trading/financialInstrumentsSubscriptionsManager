package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.component.FinancialInstrumentParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final FinancialInstrumentParametersProvider financialInstrumentParametersProvider;

    @Transactional
    public void assignUnassignedInstrumentsToActiveDataLoaders() {
        if (!financialInstrumentRepository.existsWithNoDataLoaderAssigned()) {
            log.debug("All financial instruments are assigned to data loaders.");
            return;
        }
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(
                financialInstrumentRepository.findUnassignedToAnyDataLoader(
                        FinancialInstrumentRepository.OrderBy.CREATED_ON_ASC,
                        financialInstrumentParametersProvider.getRecommendedNumberOfFinancialInstrumentsUnassignedToAnyDataLoader()));
        List<DataLoaderModel> dataLoadersReadyForAssignmentOfFinancialInstruments = new LinkedList<>(
                dataLoaderRepository.findActiveDataLoadersWithTooLowOrNullLoadStatus(
                        DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                        dataLoaderParametersProvider.getNumberOfDataLoadersHandledByManagerInOneCycle()));
        log.info("List of Data Loaders ready for assignment of Financial Instruments {}", dataLoadersReadyForAssignmentOfFinancialInstruments.toString());
        assignFIsToDLs(unassignedFIs, dataLoadersReadyForAssignmentOfFinancialInstruments);
    }

    private void assignFIsToDLs(List<FinancialInstrumentModel> financialInstrumentModelList,
                                List<DataLoaderModel> dataLoaderModelList) {
        if (financialInstrumentModelList.isEmpty() || dataLoaderModelList.isEmpty()) {
            log.debug("Financial Instrument list or Data Loaders list is empty. Cannot assign FIs to DLs.");
            return;
        }

        for (int i = 0; i < financialInstrumentModelList.size(); i++) {
            FinancialInstrumentModel financialInstrument = financialInstrumentModelList.get(i);
            DataLoaderModel dataLoader = dataLoaderModelList.get(i % dataLoaderModelList.size());
            financialInstrument.setDataLoaderId(dataLoader.getId());
            log.debug("Assigned Data Loader with id {} to Financial Instrument {}, id={}",
                    dataLoader.getId(), financialInstrument.getName(), financialInstrument.getId());
            financialInstrumentService.update(financialInstrument);
        }
    }

}
