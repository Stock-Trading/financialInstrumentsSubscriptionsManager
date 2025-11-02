package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
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
    private final DataLoaderParametersProvider dataLoaderParametersProvider;

    @Transactional
    public void unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus() {
        //TODO dodać timestamp wykonania tej operacji
        log.info("Retrieving IDs of Data Loaders with Too High status and Active flag set to true");
        List<Long> idsOfDataLoadersWithTooHighBalance = dataLoaderRepository.findIdByLoadStatusAndActive(
                        DataLoaderModel.Status.TOO_HIGH,
                        true,
                        DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC,
                        dataLoaderParametersProvider.getNumberOfDataLoadersHandledByManagerInOneCycle())
                .stream()
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
        List<Long> financialInstrumentModelsIdsToNullifyDataLoaders = financialInstrumentModels.stream()
                .skip(numberOfFinancialInstrumentsToSkip)
                .map(FinancialInstrumentModel::getId)
                .toList();
        log.debug("Unassigning Financial Instruments with ids={} from Data Loader id={}",
                financialInstrumentModelsIdsToNullifyDataLoaders.toArray(),
                idOfDataLoader);
        int numberOfAffectedRows = financialInstrumentRepository.detachDataLoaderBasedOnIds(financialInstrumentModelsIdsToNullifyDataLoaders);
        log.debug("Unassigned Data Loader (id={}) from {} Financial Instruments", idOfDataLoader, numberOfAffectedRows);
    }
}
