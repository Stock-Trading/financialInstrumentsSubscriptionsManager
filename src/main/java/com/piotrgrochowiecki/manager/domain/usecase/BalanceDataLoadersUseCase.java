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

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//@Log4j2
//@Component
//@RequiredArgsConstructor
//public class BalanceDataLoadersUseCase {
//
//    //TODO wysłać klasę Mateuszowi emailem
//
//    private final DataLoaderRepository dataLoaderRepository;
//    private final FinancialInstrumentRepository financialInstrumentRepository;
//    private final DataLoaderParametersProvider dataLoaderParametersProvider;
//
//    @Transactional
//    public void balanceDataLoadersLeastConnectionsAlg() {
//        //get list of data loaders with loadStatus=TOO_LOW and readyForAssignmentOfFinancialInstruments=true (order by lastInstantOfFinancialInstrumentsAssignment ASC)
//        //get list of data loaders with loadStatus=TOO_HIGH and readyForAssignmentOfFinancialInstruments=true (order by lastInstantOfFinancialInstrumentsAssignment ASC)
//        //count total number of available spots in all data loaders with loadStatus=TOO_LOW (up to loadStatus=BALANCED) (use dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader)
//        //retrieve ids of FIs from dataLoaders with TOO_HIGH
//
//        List<DataLoaderModel> dataLoadersTooLowBalance = dataLoaderRepository.findBasedOnLoadStatusAndReadinessForAssignmentOfFinancialInstruments(DataLoaderModel.Status.TOO_LOW,
//                        true,
//                        DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC,
//                        3)//TODO pomyśleć nad przeniesieniem jako parametr
//                .stream()
//                .toList();
//
//        List<Long> idsOfDataLoadersWithTooLowBalance = dataLoadersTooLowBalance.stream()
//                .map(DataLoaderModel::getId)
//                .toList();
//
//        List<DataLoaderModel> dataLoadersTooHighBalance = dataLoaderRepository.findBasedOnLoadStatusAndReadinessForAssignmentOfFinancialInstruments(DataLoaderModel.Status.TOO_HIGH,
//                        true,
//                        DataLoaderRepository.OrderBy.LAST_INSTANT_OF_FINANCIAL_INSTRUMENTS_ASSIGNMENT_ASC,
//                        3)//TODO pomyśleć nad przeniesieniem jako parametr
//                .stream()
//                .toList();
//
//        List<Long> idsOfDataLoadersWithTooHighBalance = dataLoadersTooHighBalance.stream()
//                .map(DataLoaderModel::getId)
//                .toList();
//
//        List<FinancialInstrumentModel> financialInstrumentModelsFromDataLoadersWithTooHighLoad = new ArrayList<>();
//        for (Long id : idsOfDataLoadersWithTooHighBalance) {
//            List<FinancialInstrumentModel> financialInstrumentModels = financialInstrumentRepository.findByDataLoaderId(id);
//            financialInstrumentModelsFromDataLoadersWithTooHighLoad.addAll(financialInstrumentModels);
//        }
//
//        int numberOfAvailableSpacesForFinancialInstrumentsInDataLoadersWithTooLowStatus = 0;
//
//        List<FinancialInstrumentModel> financialInstrumentModelsFromDataLoadersWithTooLowLoad = new ArrayList<>();
//        for (Long id : idsOfDataLoadersWithTooLowBalance) {
//            List<FinancialInstrumentModel> financialInstrumentModels = financialInstrumentRepository.findByDataLoaderId(id);
//            int sizeOfTheList = financialInstrumentModels.size();
//            int availableSpacesInGivenDataLoader = Math.subtractExact(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader(), sizeOfTheList);
//            numberOfAvailableSpacesForFinancialInstrumentsInDataLoadersWithTooLowStatus += availableSpacesInGivenDataLoader;
//            financialInstrumentModelsFromDataLoadersWithTooLowLoad.addAll(financialInstrumentModels);
//        }
//
//        log.info("Total number of available spaces in Data Loaders with load status Too Low is {}", numberOfAvailableSpacesForFinancialInstrumentsInDataLoadersWithTooLowStatus);
//
//        int counter = 0;
//        for (FinancialInstrumentModel financialInstrumentModel : financialInstrumentModelsFromDataLoadersWithTooHighLoad) {
//            DataLoaderModel dataLoader = dataLoadersTooHighBalance.getFirst();
//            long dataLoaderId = dataLoader.getId();
//            financialInstrumentModel.setDataLoaderId(dataLoaderId);
//            dataLoadersTooHighBalance.removeFirst();
//            financialInstrumentRepository.save(financialInstrumentModel);
//            counter++;
//
//        }
//
//        //TODO wyznaczyć listę instrumentów finansowych do przeniesienia od data loaderów TOO HIGH do TOO LOW:
//        // policz "avaliableSpaces" (dla każdego DL o TOO_LOW odejmij faktyczną liczbę FI od rekomendowanej liczby FIperDL i zsumuj)
//        // następnie weź z listy financialInstrumentModelsFromDataLoadersWithTooHighLoad FIs i przypisuj do DL (status TOO LOW) aż do rekomendowanego limitu
//        // pamięaj o zapisaniu modyfikacji
//    }
//
//    //OLD CODE BELOW
//
//    @Transactional
//    public void balanceDataLoaders() {
//        List<DataLoaderModel> allDataLoaders = dataLoaderRepository.findActiveAndUnhandledDataLoaders(
//                        Duration.ofMillis(dataLoaderParametersProvider.getActiveThresholdMilliseconds()),
//                        Duration.ofMillis(dataLoaderParametersProvider.getReadyForHandlingThresholdMilliseconds()),
//                        DataLoaderRepository.OrderBy.LAST_CONNECTED_ON_ASC,
//                        dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
//                .stream()
//                .toList();
//
//        List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended = allDataLoaders.stream()
//                .filter(this::doesHaveMoreFIsThanRecommended)
//                .toList();
//
//        List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended = allDataLoaders.stream()
//                .filter(this::doesHaveLessFIsThanRecommended)
//                .toList();
//
//        int totalNumberOfFreeSpots = computeNumberOfFreeSpots(dataLoadersWithLessFIsThanRecommended);
//
//        List<FinancialInstrumentModel> fIsToBeReassigned = getFIsToBeReassigned(dataLoadersWithMoreFIsThanRecommended,
//                totalNumberOfFreeSpots);
//
//        reassign(dataLoadersWithLessFIsThanRecommended, fIsToBeReassigned);
//    }
//
//    private boolean doesHaveMoreFIsThanRecommended(DataLoaderModel dataLoaderModel) {
//        return dataLoaderModel.
//
//        return dataLoaderModel.getFinancialInstrumentModelCollection()
//                //TODO zastąpić pobieraniem listy instrumentów
//                // finansowych o danym id DataLoadera. Id dataLoaderów pobrać na podstawie statusu readyForAssignmentOfFinancialInstruments
//                .size() > dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader();
//    }
//
//    private boolean doesHaveLessFIsThanRecommended(DataLoaderModel dataLoaderModel) {
//        return dataLoaderModel.getFinancialInstrumentModelCollection()
//                .size() < dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader();
//    }
//
//    private int computeNumberOfFreeSpots(List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended) {
//        int totalNumberOfFreeSpots = 0;
//        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
//            int numberOfFIs = dataLoader.getFinancialInstrumentModelCollection()
//                    .size();
//            int freeSpots = Math.subtractExact(
//                    dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader(),
//                    numberOfFIs);
//            totalNumberOfFreeSpots += freeSpots;
//        }
//        return totalNumberOfFreeSpots;
//    }
//
//    //TODO rozważyć dwa oddzielne use casey, wywoływane często
//    private List<FinancialInstrumentModel> getFIsToBeReassigned(List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended,
//                                                                int totalNumberOfFreeSpots) {
//        List<FinancialInstrumentModel> fIsToBeReassigned = new ArrayList<>();
//        //1. określ nadmiarowe instrumenty finansowe danego data loadera
//        //2. dodaj powyższe instrumenty do jednej wspólnej listy
//        //3. usuń nadmiarowy instrument finansowy z danego data loadera, jeśli został przekazany do powyższej listy
//
//        //TODO rozbić kompletnie ten use case na dwa prostsze (lub jeden ale prostszy): jeden nullujący pole dataLoaderId encji FinancialInstrument
//        // drugi ustawiający nowe pole dataLoaderId. Wtedy unikamy iterowania po kolekcjach i synchronizacji stanu w kodzie i bazie danych
//        while (fIsToBeReassigned.size() < totalNumberOfFreeSpots) {
//            boolean added = false;
//            for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
//                List<FinancialInstrumentModel> originalCollectionOfFIs = new LinkedList<>(dataLoader.getFinancialInstrumentModelCollection());
//                List<FinancialInstrumentModel> excessiveFIs = originalCollectionOfFIs
//                        .stream()
//                        .skip(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader())
//                        .toList();
//
//                for (FinancialInstrumentModel fi : excessiveFIs) {
//                    if (fIsToBeReassigned.size() < totalNumberOfFreeSpots) {
//                        fIsToBeReassigned.add(fi);
//                        added = true;
//                        originalCollectionOfFIs.remove(fi);
//                        dataLoader.setFinancialInstrumentModelCollection(originalCollectionOfFIs);
//                    } else {
//                        break;
//                    }
//                }
//                if (!added) break;
//                dataLoaderRepository.save(dataLoader);
//            }
//            if (!added) break;
//        }
//        return fIsToBeReassigned;
//    }
//
//    private void reassign(List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended,
//                          List<FinancialInstrumentModel> fIsToBeReassigned) {
//        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
//            List<FinancialInstrumentModel> fIsOfGivenDataLoader = new ArrayList<>(dataLoader.getFinancialInstrumentModelCollection()
//                    .stream()
//                    .toList());
//
//            while (fIsOfGivenDataLoader.size() < dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()) {
//                if (fIsToBeReassigned.isEmpty()) {
//                    break;
//                }
//                fIsOfGivenDataLoader.add(fIsToBeReassigned.getFirst());
//                fIsToBeReassigned.remove(0);
//            }
//            dataLoader.setFinancialInstrumentModelCollection(fIsOfGivenDataLoader);
//            dataLoaderRepository.save(dataLoader);
//        }
//    }
//}
