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

    private final DataLoaderRepository dataLoaderRepository;
    private final TimeService timeService;
    private final ParametersProvider parametersProvider;

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
        return update(dataLoaderModel);
    }

    /**
     * Checks for last check-in time of DataLoader. If that time is longer then specified threshold,
     * unassigns FinancialInstrument from it and sets its Active property to false.
     */
    @Transactional
    public void checkActiveState() {
        Collection<DataLoaderModel> dataLoaders = dataLoaderRepository.findActiveDataLoaders(
                DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()
        );
        dataLoaders.forEach(this::checkIfQualifiesAsInactiveAndUpdate);
    }

    private void checkIfQualifiesAsInactiveAndUpdate(DataLoaderModel dataLoader) {
        Instant lastInstantCountingAsHealthy = timeService.getInstantUTC()
                .minus(Duration.ofMillis(parametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()));
        if (checkIfQualifiesAsUnhealthy(dataLoader, lastInstantCountingAsHealthy)) {
            setConditionsOfUnhealthyAndUpdate(dataLoader);
        }
    }

    private boolean checkIfQualifiesAsUnhealthy(DataLoaderModel dataLoader, Instant lastInstantCountingAsHealthy) {
        if (dataLoader.getLastConnectedOn().isBefore(lastInstantCountingAsHealthy)) {
            log.debug("Data Loader id={}, uuid={} last connected on {}, which is before last instant counting" +
                            " as healthy. Setting its loadStatus and Financial Instrument Collection to null and" +
                            " Active to false. Threshold of healthy is set to {} milliseconds",
                    dataLoader.getId(), dataLoader.getUuid(), dataLoader.getLastConnectedOn(),
                    parametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds());
            return true;
        }
        return false;
    }

    private void setConditionsOfUnhealthyAndUpdate(DataLoaderModel dataLoader) {
        dataLoader.setFinancialInstrumentModelCollection(null);
        dataLoader.setLoadStatus(null);
        dataLoader.setActive(false);
        dataLoader.setReadyForHandling(false);
        update(dataLoader);
    }

    @Transactional
    public void checkReadyForHandlingStatus() {
        Collection<DataLoaderModel> dataLoaders = dataLoaderRepository.findReadyForHandling(
                DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()
        );
        dataLoaders.forEach(this::checkReadyForHandlingStatusAndUpdate);
    }

    private void checkReadyForHandlingStatusAndUpdate(DataLoaderModel dataLoader) {
        Instant lastInstantCountingAsReadyForHandling = timeService.getInstantUTC()
                .minus(
                        Duration.ofMillis(
                                parametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()
                        )
                );
        if (dataLoader.getLastConnectedOn()
                .isBefore(lastInstantCountingAsReadyForHandling)) {
            log.info("""
                            Data Loader id={}, uuid={} last connected on {}, which is before last point in time
                            counting as Ready for Handling. Setting its readyForHandling field to FALSE.
                            Threshold of ready for handling is set to {} milliseconds
                            """,
                    dataLoader.getId(),
                    dataLoader.getUuid(),
                    dataLoader.getLastConnectedOn(),
                    parametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()
            );
            dataLoader.setReadyForHandling(false);
            update(dataLoader);
        }
    }

    @Transactional
    public void checkLoadStatus() {
        List<DataLoaderModel> dataLoaderModelList = dataLoaderRepository.findActiveDataLoaders(
                        DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
                        parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
                .stream()
                .toList();
        dataLoaderModelList.forEach(this::checkAndUpdateLoadStatus);
    }

    private void checkAndUpdateLoadStatus(DataLoaderModel dataLoader) {
        int numberOfFIsAssigned = dataLoader.getFinancialInstrumentModelCollection().size();
        DataLoaderModel.Status loadStatus;
        if (numberOfFIsAssigned == parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
            loadStatus = DataLoaderModel.Status.BALANCED;
        } else if (numberOfFIsAssigned < parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
            loadStatus = DataLoaderModel.Status.TOO_LOW;
        } else {
            loadStatus = DataLoaderModel.Status.TOO_HIGH;
        }
        dataLoader.setLoadStatus(loadStatus);
        dataLoader.setLastHandledOn(timeService.getInstantUTC()); //updates time of handling, so other services retrieve records with "oldest" lastHandledOn field
        log.debug("Data Loader with id {} has {} Financial Instruments assigned to it and its load status is {}." +
                        " Number of recommended financial instruments per data loader is {}.", dataLoader.getId(),
                numberOfFIsAssigned, loadStatus,
                parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader());
        update(dataLoader);
    }

    @Transactional
    public void balanceDataLoaders() {
        List<DataLoaderModel> allDataLoaders = getActiveAndUnhandledDataLoaders()
                .stream()
                .toList();

        List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended = allDataLoaders.stream()
                .filter(dataLoaderModel ->
                        dataLoaderModel.getFinancialInstrumentModelCollection().size()
                                > parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
                .toList();

        List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended = allDataLoaders.stream()
                .filter(dataLoaderModel ->
                        dataLoaderModel.getFinancialInstrumentModelCollection().size()
                                <= parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
                .toList();

        int totalNumberOfFreeSpots = 0;
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            int numberOfFIs = dataLoader.getFinancialInstrumentModelCollection()
                    .size();
            int freeSpots = Math.subtractExact(
                    parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader(),
                    numberOfFIs
            );
            totalNumberOfFreeSpots += freeSpots;
        }

        List<FinancialInstrumentModel> fIsToBeReassigned = new ArrayList<>();
        while (fIsToBeReassigned.size() <= totalNumberOfFreeSpots) {
            for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
                List<FinancialInstrumentModel> temporaryListOfFIs = dataLoader.getFinancialInstrumentModelCollection()
                        .stream()
                        .toList();
                for (int i = parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader(); i < temporaryListOfFIs.size() - 1; i++) {
                    fIsToBeReassigned.add(temporaryListOfFIs.get(i));
                }
            }
        }

        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            List<FinancialInstrumentModel> fIsOfGivenDataLoader = new ArrayList<>(dataLoader.getFinancialInstrumentModelCollection()
                    .stream()
                    .toList());
            while (fIsOfGivenDataLoader.size() <= parametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
                fIsOfGivenDataLoader.add(fIsToBeReassigned.getFirst());
            }
            dataLoader.setFinancialInstrumentModelCollection(fIsOfGivenDataLoader);
        }
    }

    public Collection<DataLoaderModel> getActiveAndUnhandledDataLoaders() {
        return dataLoaderRepository.find5ActiveAndUnhandledDataLoaders(
                Duration.ofMillis(parametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds()),
                Duration.ofMillis(parametersProvider.getTimeThresholdOfHandlingReadinessMilliseconds())
        );
    }

    public DataLoaderModel getByUuid(String dataLoaderUuid) {
        return dataLoaderRepository.findByUuid(dataLoaderUuid);
    }

}
