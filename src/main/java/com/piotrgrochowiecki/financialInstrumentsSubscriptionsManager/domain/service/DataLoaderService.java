package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception.ModelAlreadyExistsException;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception.ModelNotFoundException;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class DataLoaderService {

//    @Value("${dataloader.lastCheckInHealthThreshold.milisec}")
    private final Integer TIME_THRESHOLD_OF_HEALTH_MILS = 10_000;
//    @Value("${dataloader.recommendedNumberOfFinancialInstruments}")
    private final Integer RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS = 5;

    private final DataLoaderRepository dataLoaderRepository;
    private final TimeService timeService;

    private static final Duration TIME_THRESHOLD_OF_HANDLING_READINESS = Duration.ofSeconds(15);

    @Transactional
    public DataLoaderModel register(String dataLoaderUuid) {
        if (dataLoaderRepository.existsByUuid(dataLoaderUuid)) {
            log.error("Data loader with uuid {} has already been registered", dataLoaderUuid);
            throw new ModelAlreadyExistsException("Data loader with uuid " + dataLoaderUuid + " has already been registered");
        }
        DataLoaderModel dataLoaderModel = new DataLoaderModel(null, dataLoaderUuid, timeService.getInstantUTC(), null, null);
        return dataLoaderRepository.save(dataLoaderModel);
    }

    @Transactional
    public DataLoaderModel checkIn(String dataLoaderUuid) {
        if (dataLoaderRepository.existsByUuid(dataLoaderUuid)) {
            dataLoaderRepository.updateDataLoaderLastConnectedOnTime(dataLoaderUuid, timeService.getInstantUTC());
            return dataLoaderRepository.findByUuid(dataLoaderUuid);
        } else {
            log.error("Did not find data loader with uuid {}", dataLoaderUuid);
            throw new ModelNotFoundException("Did not find data loader with uuid " + dataLoaderUuid);
        }
    }

    @Transactional
    public void update(DataLoaderModel dataLoaderModel) {
        if (Objects.isNull(dataLoaderModel.getId())) {
            throw new RuntimeException("Cannot update Data Loader as its id is null");
        }
        dataLoaderRepository.save(dataLoaderModel);
    }

    /**
     * Regular method that checks for last check-in time of DataLoader. If that time is longer then specified threshold,
     * unassigns FinancialInstrument from it.
     */
    @Transactional
    @Scheduled(fixedDelay = 3000)
    void checkHealth() {
        log.debug("Running regular data loaders health check");
        Collection<DataLoaderModel> inactiveDataLoaders = get5InactiveDataLoaders();
        log.debug("Collection of inactive data loaders has {} elements in it", inactiveDataLoaders.size());
        inactiveDataLoaders.forEach(dataLoader -> {
            dataLoader.setFinancialInstrumentModelCollection(null);
            update(dataLoader);
        });
    }

    @Transactional
    @Scheduled(fixedDelay = 17_500)
    private void rebalanceDataLoaders() {
        log.info("Running regular rebalance of Data Loaders");
        List<DataLoaderModel> allDataLoaders = getAllActiveAndUnhandledDataLoaders().stream().toList();

        List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended = allDataLoaders.stream()
                .filter(dataLoaderModel -> dataLoaderModel.getFinancialInstrumentModelCollection().size() > RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS)
                .toList();

        List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended = allDataLoaders.stream()
                .filter(dataLoaderModel -> dataLoaderModel.getFinancialInstrumentModelCollection().size() <= RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS)
                .toList();

        int totalNumberOfFreeSpots = 0;
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            int numberOfFIs = dataLoader.getFinancialInstrumentModelCollection().size();
            int freeSpots = Math.subtractExact(RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS, numberOfFIs);
            totalNumberOfFreeSpots=+freeSpots;
        }

        List<FinancialInstrumentModel> FIsToBeReassigned = new ArrayList<>();
        while (FIsToBeReassigned.size() <= totalNumberOfFreeSpots) {
            for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
                List<FinancialInstrumentModel> temporaryListOfFIs = dataLoader.getFinancialInstrumentModelCollection().stream().toList();
                for (int i = RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS; i < temporaryListOfFIs.size() - 1; i++) {
                    FIsToBeReassigned.add(temporaryListOfFIs.get(i));
                }
            }
        }

        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            List<FinancialInstrumentModel> FIsOfGivenDataLoader = new ArrayList<>(dataLoader.getFinancialInstrumentModelCollection().stream().toList());
            while (FIsOfGivenDataLoader.size() <= RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS) {
                FIsOfGivenDataLoader.add(FIsToBeReassigned.getFirst());
            }
            dataLoader.setFinancialInstrumentModelCollection(FIsOfGivenDataLoader);
        }
    }

    public Collection<DataLoaderModel> get5InactiveDataLoaders() {
        return dataLoaderRepository.find5InactiveDataLoaders(Duration.ofSeconds(TIME_THRESHOLD_OF_HEALTH_MILS));
    }

    public Collection<DataLoaderModel> getAllActiveDataLoaders() {
        return dataLoaderRepository.findAllActiveDataLoaders(Duration.ofSeconds(TIME_THRESHOLD_OF_HEALTH_MILS));
    }

    public Collection<DataLoaderModel> getAllActiveAndUnhandledDataLoaders() {
        return dataLoaderRepository.findAllActiveAndUnhandledDataLoaders(Duration.ofMillis(TIME_THRESHOLD_OF_HEALTH_MILS), TIME_THRESHOLD_OF_HANDLING_READINESS);
    }

    public DataLoaderModel getByUuid(String dataLoaderUuid) {
        return dataLoaderRepository.findByUuid(dataLoaderUuid);
    }

    public List<DataLoaderModel> getActiveDataLoadersWithNumberOfAssignedFinancialInstrumentsGreaterThanRecommendedOrderAsc() {
        return dataLoaderRepository.findAllWithNumberOfAssignedFinancialInstrumentsGreaterThanRecommendedAndLastConnectedOnGreaterThanOrderAscByFinancialInstrumentCount(RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS, Duration.ofMillis(TIME_THRESHOLD_OF_HEALTH_MILS));
    }

    public List<DataLoaderModel> getActiveDataLoadersWithNumberOfAssignedFinancialInstrumentsLessThanEqualRecommendedOrderAsc() {
        return dataLoaderRepository.findAllWithNumberOfAssignedFinancialInstrumentsLessThanEqualRecommendedAndLastConnectedOnGreaterThanOrderAscByFinancialInstrumentCount(RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS, Duration.ofMillis(TIME_THRESHOLD_OF_HEALTH_MILS));
    }

}
