package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class UnassignFinancialInstrumentFromAllInactiveDataLoaderUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;

    @Transactional
    public void unassignFinancialInstrumentFromInactiveDataLoader() {
        log.info("Retrieving IDs of Data Loaders with Active flag set to false");
        Collection<DataLoaderModel> collection = dataLoaderRepository.findInactiveDataLoaders();
        List<Long> idsOfDataLoadersWithFalseActiveFlag = collection
                .stream()
                .map(DataLoaderModel::getId)
                .toList();
        log.debug("Ids of Data Loaders with Active flag set to false: {}",
                Arrays.toString(idsOfDataLoadersWithFalseActiveFlag.toArray()));

        collection.forEach(model -> {
            unassignAllFinancialInstrumentsFromDataLoader(model);
            model.setLastLoadStatusUpdate(Instant.now());
            dataLoaderRepository.save(model);
        });
    }

    private void unassignAllFinancialInstrumentsFromDataLoader(DataLoaderModel model) {
        Long idOfDataLoader = model.getId();
        log.debug("Unassigning all Financial Instruments from Data Loader with id {}", idOfDataLoader);
        List<FinancialInstrumentModel> financialInstrumentModels =
                financialInstrumentRepository.findByDataLoaderId(idOfDataLoader);
        log.debug("Number of Financial Instruments assigned to Data Loader id={} is {}",
                idOfDataLoader, financialInstrumentModels.size());
        if (financialInstrumentModels.isEmpty()) {
            return;
        }
        List<Long> financialInstrumentModelsIdsToNullifyDataLoaders = financialInstrumentModels.stream()
                .map(FinancialInstrumentModel::getId)
                .toList();
        log.debug("Unassigning Financial Instruments with ids={} from Data Loader id={}",
                financialInstrumentModelsIdsToNullifyDataLoaders.toArray(),
                idOfDataLoader);
        int numberOfAffectedRows =
                financialInstrumentRepository.detachDataLoaderBasedOnIds(financialInstrumentModelsIdsToNullifyDataLoaders);
        log.debug("Unassigned Data Loader (id={}) from {} Financial Instruments",
                idOfDataLoader, numberOfAffectedRows);
    }

}
