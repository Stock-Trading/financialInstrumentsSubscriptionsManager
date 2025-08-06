package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class UnassignFinancialInstrumentFromDataLoaderUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final FinancialInstrumentService financialInstrumentService;
    private final DataLoaderParametersProvider dataLoaderParametersProvider;

    @Transactional
    public void unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus() {
        List<DataLoaderModel> dataLoadersTooHighBalance = dataLoaderRepository.findBasedOnLoadStatusAndReadinessForAssignmentOfFinancialInstruments(DataLoaderModel.Status.TOO_HIGH,
                        true,
                        DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC,
                        dataLoaderParametersProvider.getNumberOfDataLoadersHandledByManagerInOneCycle())
                .stream()
                .toList();

        List<Long> idsOfDataLoadersWithTooHighBalance = dataLoadersTooHighBalance.stream()
                .map(DataLoaderModel::getId)
                .toList();

        for (Long id : idsOfDataLoadersWithTooHighBalance) {
            unassignFinancialInstrumentsFromDataLoader(id);
        }
    }

    private void unassignFinancialInstrumentsFromDataLoader(Long idOfDataLoader) {
        List<FinancialInstrumentModel> financialInstrumentModels = financialInstrumentRepository.findByDataLoaderId(idOfDataLoader);
        //don't unassign more FIs from DL than needed
        int numberOfFinancialInstrumentsToSkip = dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader();
        financialInstrumentModels.stream()
                .skip(numberOfFinancialInstrumentsToSkip)
                .map(it -> {
                    it.setDataLoaderId(null);
                    financialInstrumentService.update(it);
                    return it;
                })
                .toList();
    }
}
