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

import java.util.Arrays;
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
        log.debug("Retrieving Data Loaders with Too High status and active flag readyForAssignmentOfFinancialInstruments");
        List<DataLoaderModel> dataLoadersTooHighBalance = dataLoaderRepository.findBasedOnLoadStatusAndReadinessForAssignmentOfFinancialInstruments(
                        DataLoaderModel.Status.TOO_HIGH,
                        true,
                        DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC,
                        dataLoaderParametersProvider.getNumberOfDataLoadersHandledByManagerInOneCycle())
                .stream()
                .toList();
        log.debug("List has {} objects", dataLoadersTooHighBalance.size());

        List<Long> idsOfDataLoadersWithTooHighBalance = dataLoadersTooHighBalance.stream()
                .map(DataLoaderModel::getId)
                .toList();
        log.debug("Ids of Data Loaders with load status Too High: {}", Arrays.toString(idsOfDataLoadersWithTooHighBalance.toArray()));

        for (Long id : idsOfDataLoadersWithTooHighBalance) {
            unassignFinancialInstrumentsFromDataLoader(id);
        }
    }

    private void unassignFinancialInstrumentsFromDataLoader(Long idOfDataLoader) {
        log.debug("Unassigning excessive Financial Instruments from Data Loader with id {}", idOfDataLoader);
        List<FinancialInstrumentModel> financialInstrumentModels = financialInstrumentRepository.findByDataLoaderId(idOfDataLoader);
        log.debug("Number of Financial Instruments assigned to Data Loader id={} is {}", idOfDataLoader, financialInstrumentModels.size());
        //don't unassign more FIs from DL than needed:
        int numberOfFinancialInstrumentsToSkip = dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader();
        log.debug("Recommended number of Financial Instruments per Data Loader is set to {}", numberOfFinancialInstrumentsToSkip);
        financialInstrumentModels.stream()
                .skip(numberOfFinancialInstrumentsToSkip)
                .map(it -> {
                    log.debug("Unassigning Financial Instrument {}, id={} from Data Loader id={}", it.getName(), it.getId(), idOfDataLoader);
                    it.setDataLoaderId(null);
                    financialInstrumentService.update(it);
                    return it;
                })
                .toList();
    }
}
