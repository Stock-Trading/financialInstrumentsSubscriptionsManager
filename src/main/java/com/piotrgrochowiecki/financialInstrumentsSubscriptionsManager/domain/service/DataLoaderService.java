package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception.DataLoaderServiceException;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception.ModelAlreadyExistsException;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class DataLoaderService {

    //    @Value("${dataloader.lastCheckInHealthThreshold.milisec}")
    private static final Integer TIME_THRESHOLD_OF_HEALTH_MILLS = 10_000;
    //    @Value("${dataloader.recommendedNumberOfFinancialInstruments}")
    private static final Integer RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS = 5;
    private static final Integer TIME_THRESHOLD_OF_HANDLING_READINESS_MILLS = 15_000;

    private final DataLoaderService self;
    private final DataLoaderRepository dataLoaderRepository;
    private final TimeService timeService;

    @Transactional
    public DataLoaderModel update(DataLoaderModel dataLoaderModel) {
        if (Objects.isNull(dataLoaderModel.getId())) {
            throw new DataLoaderServiceException("Cannot update Data Loader as its id is null");
        }
        return dataLoaderRepository.save(dataLoaderModel);
    }

    @Transactional
    public DataLoaderModel register(String dataLoaderUuid) {
        if (dataLoaderRepository.existsByUuid(dataLoaderUuid)) {
            log.error("Data loader with uuid {} has already been registered", dataLoaderUuid);
            throw new ModelAlreadyExistsException("Data loader with uuid " + dataLoaderUuid + " has already been registered");
        }
        DataLoaderModel dataLoaderModel = DataLoaderModel.builder()
                .uuid(dataLoaderUuid)
                .lastConnectedOn(timeService.getInstantUTC())
                .build();
        return dataLoaderRepository.save(dataLoaderModel);
    }

    @Transactional
    public DataLoaderModel checkIn(String dataLoaderUuid) {
        DataLoaderModel dataLoaderModel = getByUuid(dataLoaderUuid);
        dataLoaderModel.setLastConnectedOn(timeService.getInstantUTC());
        dataLoaderModel.setActive(true);
        dataLoaderModel.setReadyForHandling(true);
        return self.update(dataLoaderModel);
    }

    /**
     * Checks for last check-in time of DataLoader. If that time is longer then specified threshold,
     * unassigns FinancialInstrument from it and sets its Active property to false.
     */
    @Transactional
    public void checkActiveState() {
        Collection<DataLoaderModel> dataLoaders = getActiveDataLoaders();
        dataLoaders.forEach(this::checkIfQualifiesAsInactiveAndUpdate);
    }

    private void checkIfQualifiesAsInactiveAndUpdate(DataLoaderModel dataLoader) {
        Instant lastInstantCountingAsHealthy = timeService.getInstantUTC()
                .minus(Duration.ofMillis(TIME_THRESHOLD_OF_HANDLING_READINESS_MILLS));
        if (checkIfQualifiesAsUnhealthy(dataLoader, lastInstantCountingAsHealthy)) {
            setConditionsOfUnhealthyAndUpdate(dataLoader);
        }
    }

    private boolean checkIfQualifiesAsUnhealthy(DataLoaderModel dataLoader, Instant lastInstantCountingAsHealthy) {
        if (dataLoader.getLastConnectedOn().isBefore(lastInstantCountingAsHealthy)) {
            log.debug("Data Loader id={}, uuid={} last connected on {}, which is before last instant counting" +
                            " as healthy. Setting its loadStatus and Financial Instrument Collection to null and" +
                            " Active to false. Threshold of healthy is set to {} milliseconds",
                    dataLoader.getId(), dataLoader.getUuid(), dataLoader.getLastConnectedOn(), TIME_THRESHOLD_OF_HEALTH_MILLS);
            return true;
        }
        return false;
    }

    private void setConditionsOfUnhealthyAndUpdate(DataLoaderModel dataLoader) {
        dataLoader.setFinancialInstrumentModelCollection(null);
        dataLoader.setLoadStatus(null);
        dataLoader.setActive(false);
        dataLoader.setReadyForHandling(false);
        self.update(dataLoader);
    }

    @Transactional
    public void checkReadyForHandlingStatus() {
        Collection<DataLoaderModel> dataLoaders = getReadyForHandlingDataLoaders();
        dataLoaders.forEach(this::checkReadyForHandlingStatusAndUpdate);
    }

    private void checkReadyForHandlingStatusAndUpdate(DataLoaderModel dataLoader) {
        Instant lastInstantCountingAsReadyForHandling = timeService.getInstantUTC()
                .minus(Duration.ofMillis(TIME_THRESHOLD_OF_HANDLING_READINESS_MILLS));
        if (dataLoader.getLastConnectedOn().isBefore(lastInstantCountingAsReadyForHandling)) {
            log.debug("Data Loader id={}, uuid={} last connected on {}, which is before last instant counting" +
                            " as Ready for Handling to false. Threshold of read for handling readiness is set to {} milliseconds",
                    dataLoader.getId(), dataLoader.getUuid(), dataLoader.getLastConnectedOn(), TIME_THRESHOLD_OF_HANDLING_READINESS_MILLS);
            dataLoader.setReadyForHandling(false);
            self.update(dataLoader);
        }
    }

    @Transactional
    public void checkLoadStatus() {
        List<DataLoaderModel> dataLoaderModelList = getActiveDataLoaders().stream()
                .toList();
        dataLoaderModelList.forEach(this::checkAndUpdateLoadStatus);
    }

    private void checkAndUpdateLoadStatus(DataLoaderModel dataLoader) {
        int numberOfFIsAssigned = dataLoader.getFinancialInstrumentModelCollection().size();
        DataLoaderModel.DataLoaderLoadStatus loadStatus;
        if (numberOfFIsAssigned == RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS) {
            loadStatus = DataLoaderModel.DataLoaderLoadStatus.BALANCED;
        } else if (numberOfFIsAssigned < RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS) {
            loadStatus = DataLoaderModel.DataLoaderLoadStatus.TOO_LOW;
        } else {
            loadStatus = DataLoaderModel.DataLoaderLoadStatus.TOO_HIGH;
        }
        dataLoader.setLoadStatus(loadStatus);
        dataLoader.setLastHandledOn(timeService.getInstantUTC()); //updates time of handling, so other services retrieve records with "oldest" lastHandledOn field
        log.debug("Data Loader with id {} has {} Financial Instruments assigned to it and its load status is {}." +
                        " Number of recommended financial instruments per data loader is {}.", dataLoader.getId(),
                numberOfFIsAssigned, loadStatus, RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS);
        self.update(dataLoader);
    }

    @Transactional
    public void balanceDataLoaders() {
        List<DataLoaderModel> allDataLoaders = getActiveAndUnhandledDataLoaders().stream().toList();

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
            totalNumberOfFreeSpots += freeSpots;
        }

        List<FinancialInstrumentModel> fIsToBeReassigned = new ArrayList<>();
        while (fIsToBeReassigned.size() <= totalNumberOfFreeSpots) {
            for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
                List<FinancialInstrumentModel> temporaryListOfFIs = dataLoader.getFinancialInstrumentModelCollection().stream().toList();
                for (int i = RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS; i < temporaryListOfFIs.size() - 1; i++) {
                    fIsToBeReassigned.add(temporaryListOfFIs.get(i));
                }
            }
        }

        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            List<FinancialInstrumentModel> fIsOfGivenDataLoader = new ArrayList<>(dataLoader.getFinancialInstrumentModelCollection().stream().toList());
            while (fIsOfGivenDataLoader.size() <= RECOMMENDED_NUMBER_OF_FINANCIAL_INSTRUMENTS) {
                fIsOfGivenDataLoader.add(fIsToBeReassigned.getFirst());
            }
            dataLoader.setFinancialInstrumentModelCollection(fIsOfGivenDataLoader);
        }
    }

    public Collection<DataLoaderModel> getActiveDataLoaders() {
        return dataLoaderRepository.find5OldestAndActiveDataLoaders();
    }

    public Collection<DataLoaderModel> getReadyForHandlingDataLoaders() {
        return dataLoaderRepository.find5OldestAndReadyForHandling();
    }

    public Collection<DataLoaderModel> getActiveAndUnhandledDataLoaders() {
        return dataLoaderRepository.find5ActiveAndUnhandledDataLoaders(Duration.ofMillis(TIME_THRESHOLD_OF_HEALTH_MILLS),
                Duration.ofMillis(TIME_THRESHOLD_OF_HANDLING_READINESS_MILLS));
    }

    public DataLoaderModel getByUuid(String dataLoaderUuid) {
        return dataLoaderRepository.findByUuid(dataLoaderUuid);
    }

}
