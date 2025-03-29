package com.piotrgrochowiecki.manager.domain.usecase;

import static org.junit.jupiter.api.Assertions.*;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceDataLoadersUseCaseTest {

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private DataLoaderParametersProvider dataLoaderParametersProvider;

    @InjectMocks
    private BalanceDataLoadersUseCase balanceDataLoadersUseCase;

    private List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended;
    private List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended;

    @Test
    @DisplayName("""
            1.Given one DL with 2 FIs and one DL with 6 FIs,
            when balanceDataLoaders() is invoked,
            then 6th FI should be reassigned to first DL.
            """)
    void shouldReassignOneFI() {
        //given
        dataLoadersWithMoreFIsThanRecommended = new ArrayList<>();
        dataLoadersWithLessFIsThanRecommended = new ArrayList<>();

        DataLoaderModel dataLoaderWithMoreFIs = DataLoaderModel.builder()
                .id(1L)
                .uuid("uuid1")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(1L).name("FI1").symbol("FI1").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(2L).name("FI2").symbol("FI2").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(3L).name("FI3").symbol("FI3").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(4L).name("FI4").symbol("FI4").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(5L).name("FI5").symbol("FI5").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(6L).name("FI6").symbol("FI6").dataLoaderId(1L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs);

        DataLoaderModel dataLoaderWithLessFIs = DataLoaderModel.builder()
                .id(2L)
                .uuid("uuid2")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(7L).name("FI7").symbol("FI7").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(8L).name("FI8").symbol("FI8").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithLessFIsThanRecommended.add(dataLoaderWithLessFIs);

        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(5);
        when(dataLoaderRepository.findActiveAndUnhandledDataLoaders(any(Duration.class), any(Duration.class), any(), anyInt()))
                .thenReturn(Stream.concat(dataLoadersWithMoreFIsThanRecommended.stream(),
                        dataLoadersWithLessFIsThanRecommended.stream()).toList());

        // when
        balanceDataLoadersUseCase.balanceDataLoaders();

        // then
        for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
            assertEquals(5, dataLoader.getFinancialInstrumentModelCollection().size());
            assertEquals("FI1", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
            assertEquals("FI2", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
            assertEquals("FI3", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
            assertEquals("FI4", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
            assertEquals("FI5", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
        }
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            assertEquals(3, dataLoader.getFinancialInstrumentModelCollection().size());
            assertEquals("FI6", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
        }
        verify(dataLoaderRepository, times(2)).save(any(DataLoaderModel.class));
    }

    @Test
    @DisplayName("""
            2.Given one DL with 0 FIs and one DL with 7 FIs,
            when balanceDataLoaders() is invoked,
            then 6th and 7th FIs should be reassigned to first DL.
            """)
    void shouldReassignTwoFIs() {
        //given
        dataLoadersWithMoreFIsThanRecommended = new ArrayList<>();
        dataLoadersWithLessFIsThanRecommended = new ArrayList<>();

        DataLoaderModel dataLoaderWithMoreFIs = DataLoaderModel.builder()
                .id(1L)
                .uuid("uuid1")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(1L).name("FI1").symbol("FI1").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(2L).name("FI2").symbol("FI2").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(3L).name("FI3").symbol("FI3").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(4L).name("FI4").symbol("FI4").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(5L).name("FI5").symbol("FI5").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(6L).name("FI6").symbol("FI6").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(7L).name("FI7").symbol("FI7").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs);

        DataLoaderModel dataLoaderWithLessFIs = DataLoaderModel.builder()
                .id(2L)
                .uuid("uuid2")
                .financialInstrumentModelCollection(List.of())
                .build();
        dataLoadersWithLessFIsThanRecommended.add(dataLoaderWithLessFIs);

        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(5);
        when(dataLoaderRepository.findActiveAndUnhandledDataLoaders(any(Duration.class), any(Duration.class), any(), anyInt()))
                .thenReturn(Stream.concat(dataLoadersWithMoreFIsThanRecommended.stream(),
                        dataLoadersWithLessFIsThanRecommended.stream()).toList());

        // when
        balanceDataLoadersUseCase.balanceDataLoaders();

        // then
        for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
            assertEquals(5, dataLoader.getFinancialInstrumentModelCollection().size());
            assertEquals("FI1", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
            assertEquals("FI2", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
            assertEquals("FI3", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
            assertEquals("FI4", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
            assertEquals("FI5", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
        }
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            assertEquals(2, dataLoader.getFinancialInstrumentModelCollection().size());
            assertEquals("FI6", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
            assertEquals("FI7", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
        }
        verify(dataLoaderRepository, times(2)).save(any(DataLoaderModel.class));
    }

    @Test
    @DisplayName("""
            3.Given one DL with 0 FI, one DL with 7 FIs and one DL with 8 FIs and recommended number of FIs per DL being 5,
            when balanceDataLoaders() is invoked,
            then FIs should be reassigned from 2nd and 3rd to 1st DL.
            """)
    void shouldReassignFiveFIs() {
        //given
        dataLoadersWithMoreFIsThanRecommended = new ArrayList<>();
        dataLoadersWithLessFIsThanRecommended = new ArrayList<>();

        DataLoaderModel dataLoaderWithMoreFIs = DataLoaderModel.builder()
                .id(1L)
                .uuid("uuid1")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(1L).name("FI1").symbol("FI1").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(2L).name("FI2").symbol("FI2").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(3L).name("FI3").symbol("FI3").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(4L).name("FI4").symbol("FI4").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(5L).name("FI5").symbol("FI5").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(6L).name("FI6").symbol("FI6").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(7L).name("FI7").symbol("FI7").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs);

        DataLoaderModel dataLoaderWithMoreFIs2 = DataLoaderModel.builder()
                .id(2L)
                .uuid("uuid2")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(8L).name("FI8").symbol("FI8").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(9L).name("FI9").symbol("FI9").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(10L).name("FI10").symbol("FI10").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(11L).name("FI11").symbol("FI11").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(12L).name("FI12").symbol("FI12").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(13L).name("FI13").symbol("FI13").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(14L).name("FI14").symbol("FI14").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(15L).name("FI15").symbol("FI15").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs2);

        DataLoaderModel dataLoaderWithLessFIs = DataLoaderModel.builder()
                .id(3L)
                .uuid("uuid3")
                .financialInstrumentModelCollection(List.of())
                .build();
        dataLoadersWithLessFIsThanRecommended.add(dataLoaderWithLessFIs);

        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(5);
        when(dataLoaderRepository.findActiveAndUnhandledDataLoaders(any(Duration.class), any(Duration.class), any(), anyInt()))
                .thenReturn(Stream.concat(dataLoadersWithMoreFIsThanRecommended.stream(),
                        dataLoadersWithLessFIsThanRecommended.stream()).toList());

        // when
        balanceDataLoadersUseCase.balanceDataLoaders();

        // then
        for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
            if (dataLoader.getUuid().equals("uuid1")) {
                assertEquals(5, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI1", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI2", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI3", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI4", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI5", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
            }
            if (dataLoader.getUuid().equals("uuid2")) {
                assertEquals(5, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI8", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI9", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI10", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI11", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI12", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
            }

        }
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            assertEquals(5, dataLoader.getFinancialInstrumentModelCollection().size());
            assertEquals("FI6", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
            assertEquals("FI7", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
            assertEquals("FI13", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
            assertEquals("FI14", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
            assertEquals("FI15", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
        }
        verify(dataLoaderRepository, times(3)).save(any(DataLoaderModel.class));
    }

    @Test
    @DisplayName("""
            4.Given one DL with 4 FIs, one DL with 7 FIs and one DL with 8 FIs and recommended number of FIs per DL being 5,
            when balanceDataLoaders() is invoked,
            then 6th FI should be reassigned from 2nd to 1st DL.
            """)
    void shouldReassignOneFIFromOneDLoutOfTwo() {
        //given
        dataLoadersWithMoreFIsThanRecommended = new ArrayList<>();
        dataLoadersWithLessFIsThanRecommended = new ArrayList<>();

        DataLoaderModel dataLoaderWithMoreFIs = DataLoaderModel.builder()
                .id(1L)
                .uuid("uuid1")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(1L).name("FI1").symbol("FI1").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(2L).name("FI2").symbol("FI2").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(3L).name("FI3").symbol("FI3").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(4L).name("FI4").symbol("FI4").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(5L).name("FI5").symbol("FI5").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(6L).name("FI6").symbol("FI6").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(7L).name("FI7").symbol("FI7").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs);

        DataLoaderModel dataLoaderWithMoreFIs2 = DataLoaderModel.builder()
                .id(2L)
                .uuid("uuid2")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(8L).name("FI8").symbol("FI8").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(9L).name("FI9").symbol("FI9").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(10L).name("FI10").symbol("FI10").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(11L).name("FI11").symbol("FI11").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(12L).name("FI12").symbol("FI12").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(13L).name("FI13").symbol("FI13").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(14L).name("FI14").symbol("FI14").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(15L).name("FI15").symbol("FI15").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs2);

        DataLoaderModel dataLoaderWithLessFIs = DataLoaderModel.builder()
                .id(3L)
                .uuid("uuid3")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(16L).name("FI16").symbol("FI16").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(17L).name("FI17").symbol("FI17").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(18L).name("FI18").symbol("FI18").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(19L).name("FI19").symbol("FI19").dataLoaderId(3L).build()
                ))
                .build();
        dataLoadersWithLessFIsThanRecommended.add(dataLoaderWithLessFIs);

        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(5);
        when(dataLoaderRepository.findActiveAndUnhandledDataLoaders(any(Duration.class), any(Duration.class), any(), anyInt()))
                .thenReturn(Stream.concat(dataLoadersWithMoreFIsThanRecommended.stream(),
                        dataLoadersWithLessFIsThanRecommended.stream()).toList());

        // when
        balanceDataLoadersUseCase.balanceDataLoaders();

        // then
        for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
            if (dataLoader.getUuid().equals("uuid1")) {
                assertEquals(6, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI1", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI2", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI3", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI4", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI5", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
                assertEquals("FI7", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(5).getName());
            }
            if (dataLoader.getUuid().equals("uuid2")) {
                assertEquals(8, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI8", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI9", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI10", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI11", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI12", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
                assertEquals("FI13", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(5).getName());
                assertEquals("FI14", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(6).getName());
                assertEquals("FI15", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(7).getName());
            }

        }
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            assertEquals(5, dataLoader.getFinancialInstrumentModelCollection().size());
            assertEquals("FI16", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
            assertEquals("FI17", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
            assertEquals("FI18", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
            assertEquals("FI19", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
            assertEquals("FI6", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName()); //newly added FI from 2nd DL
        }
        verify(dataLoaderRepository, times(3)).save(any(DataLoaderModel.class));
        //TODO spróbować zrobić tak, aby nie zapisywać DLa, jeśli nie podelgał zmianom, w tym teście powinny być 2x .save(), a nie 3x
    }

    @Test
    @DisplayName("""
            5.Given one DL with 6 FIs, one DL with 7 FIs and one DL with 8 FIs and recommended number of FIs per DL being 5,
            when balanceDataLoaders() is invoked,
            then no FI should be reassigned.
            """)
    void shouldNotReassignAnyFIs() {
        //given
        dataLoadersWithMoreFIsThanRecommended = new ArrayList<>();
        dataLoadersWithLessFIsThanRecommended = List.of();

        DataLoaderModel dataLoaderWithMoreFIs = DataLoaderModel.builder()
                .id(1L)
                .uuid("uuid1")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(1L).name("FI1").symbol("FI1").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(2L).name("FI2").symbol("FI2").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(3L).name("FI3").symbol("FI3").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(4L).name("FI4").symbol("FI4").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(5L).name("FI5").symbol("FI5").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(6L).name("FI6").symbol("FI6").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(7L).name("FI7").symbol("FI7").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs);

        DataLoaderModel dataLoaderWithMoreFIs2 = DataLoaderModel.builder()
                .id(2L)
                .uuid("uuid2")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(8L).name("FI8").symbol("FI8").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(9L).name("FI9").symbol("FI9").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(10L).name("FI10").symbol("FI10").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(11L).name("FI11").symbol("FI11").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(12L).name("FI12").symbol("FI12").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(13L).name("FI13").symbol("FI13").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(14L).name("FI14").symbol("FI14").dataLoaderId(2L).build(),
                        FinancialInstrumentModel.builder().id(15L).name("FI15").symbol("FI15").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs2);

        DataLoaderModel dataLoaderWithMoreFIs3 = DataLoaderModel.builder()
                .id(3L)
                .uuid("uuid3")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(16L).name("FI16").symbol("FI16").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(17L).name("FI17").symbol("FI17").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(18L).name("FI18").symbol("FI18").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(19L).name("FI19").symbol("FI19").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(20L).name("FI20").symbol("FI20").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(21L).name("FI21").symbol("FI21").dataLoaderId(3L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs3);

        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(5);
        when(dataLoaderRepository.findActiveAndUnhandledDataLoaders(any(Duration.class), any(Duration.class), any(), anyInt()))
                .thenReturn(Stream.concat(dataLoadersWithMoreFIsThanRecommended.stream(),
                        dataLoadersWithLessFIsThanRecommended.stream()).toList());

        // when
        balanceDataLoadersUseCase.balanceDataLoaders();

        // then
        for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
            if (dataLoader.getUuid().equals("uuid1")) {
                assertEquals(7, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI1", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI2", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI3", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI4", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI5", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
                assertEquals("FI6", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(5).getName());
                assertEquals("FI7", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(6).getName());
            }
            if (dataLoader.getUuid().equals("uuid2")) {
                assertEquals(8, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI8", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI9", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI10", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI11", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI12", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
                assertEquals("FI13", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(5).getName());
                assertEquals("FI14", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(6).getName());
                assertEquals("FI15", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(7).getName());
            }
            if (dataLoader.getUuid().equals("uuid3")) {
                assertEquals(6, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI16", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI17", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI18", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI19", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI20", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
                assertEquals("FI21", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(5).getName());
            }
            assertEquals(0, dataLoadersWithLessFIsThanRecommended.size());
            verify(dataLoaderRepository, times(0)).save(any(DataLoaderModel.class));
        }
    }

    @Test
    @DisplayName("""
            6.Given one DL with 1 FI, one DL with 2 FIs and one DL with 20 FIs and recommended number of FIs per DL being 5,
            when balanceDataLoaders() is invoked,
            then FIs should be reassigned from 3rd to 1st and 2nd DLs.
            """)
    void shouldReassignSevenFIs() {
        //given
        dataLoadersWithMoreFIsThanRecommended = new ArrayList<>();
        dataLoadersWithLessFIsThanRecommended = new ArrayList<>();

        DataLoaderModel dataLoaderWithMoreFIs = DataLoaderModel.builder()
                .id(1L)
                .uuid("uuid1")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(1L).name("FI1").symbol("FI1").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(2L).name("FI2").symbol("FI2").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(3L).name("FI3").symbol("FI3").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(4L).name("FI4").symbol("FI4").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(5L).name("FI5").symbol("FI5").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(6L).name("FI6").symbol("FI6").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(7L).name("FI7").symbol("FI7").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(8L).name("FI8").symbol("FI8").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(9L).name("FI9").symbol("FI9").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(10L).name("FI10").symbol("FI10").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(11L).name("FI11").symbol("FI11").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(12L).name("FI12").symbol("FI12").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(13L).name("FI13").symbol("FI13").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(14L).name("FI14").symbol("FI14").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(15L).name("FI15").symbol("FI15").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(16L).name("FI16").symbol("FI16").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(17L).name("FI17").symbol("FI17").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(18L).name("FI18").symbol("FI18").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(19L).name("FI19").symbol("FI19").dataLoaderId(1L).build(),
                        FinancialInstrumentModel.builder().id(20L).name("FI20").symbol("FI20").dataLoaderId(1L).build()
                ))
                .build();
        dataLoadersWithMoreFIsThanRecommended.add(dataLoaderWithMoreFIs);

        DataLoaderModel dataLoaderWithLessFIs = DataLoaderModel.builder()
                .id(2L)
                .uuid("uuid2")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(21L).name("FI21").symbol("FI21").dataLoaderId(2L).build()
                ))
                .build();
        dataLoadersWithLessFIsThanRecommended.add(dataLoaderWithLessFIs);

        DataLoaderModel dataLoaderWithLessFIs2 = DataLoaderModel.builder()
                .id(3L)
                .uuid("uuid3")
                .financialInstrumentModelCollection(List.of(
                        FinancialInstrumentModel.builder().id(22L).name("FI22").symbol("FI22").dataLoaderId(3L).build(),
                        FinancialInstrumentModel.builder().id(23L).name("FI23").symbol("FI23").dataLoaderId(3L).build()
                ))
                .build();
        dataLoadersWithLessFIsThanRecommended.add(dataLoaderWithLessFIs2);

        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(5);
        when(dataLoaderRepository.findActiveAndUnhandledDataLoaders(any(Duration.class), any(Duration.class), any(), anyInt()))
                .thenReturn(Stream.concat(dataLoadersWithMoreFIsThanRecommended.stream(),
                        dataLoadersWithLessFIsThanRecommended.stream()).toList());

        // when
        balanceDataLoadersUseCase.balanceDataLoaders();

        // then
        for (DataLoaderModel dataLoader : dataLoadersWithMoreFIsThanRecommended) {
            if (dataLoader.getUuid().equals("uuid1")) {
                assertEquals(13, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI1", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI2", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI3", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI4", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI5", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
                assertEquals("FI13", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(5).getName());
                assertEquals("FI14", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(6).getName());
                assertEquals("FI15", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(7).getName());
                assertEquals("FI16", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(8).getName());
                assertEquals("FI17", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(9).getName());
                assertEquals("FI18", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(10).getName());
                assertEquals("FI19", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(11).getName());
                assertEquals("FI20", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(12).getName());

            }
        }
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            if (dataLoader.getUuid().equals("uuid2")) {
                assertEquals(5, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI21", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI6", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI7", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI8", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI9", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
            }
            if (dataLoader.getUuid().equals("uuid3")) {
                assertEquals(5, dataLoader.getFinancialInstrumentModelCollection().size());
                assertEquals("FI22", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(0).getName());
                assertEquals("FI23", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(1).getName());
                assertEquals("FI10", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(2).getName());
                assertEquals("FI11", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(3).getName());
                assertEquals("FI12", dataLoader.getFinancialInstrumentModelCollection().stream().toList().get(4).getName());
            }
        }
        verify(dataLoaderRepository, times(3)).save(any(DataLoaderModel.class));
    }

    @Test
    void testNoDataLoaders() {
        // Arrange
        when(dataLoaderRepository.findActiveAndUnhandledDataLoaders(any(Duration.class), any(Duration.class), any(), anyInt())).thenReturn(new ArrayList<>());

        // Act and Assert
        balanceDataLoadersUseCase.balanceDataLoaders();
        verify(dataLoaderRepository, never()).save(any(DataLoaderModel.class));
    }

    @Test
    void testNotEnoughFinancialInstruments() {
        // Arrange
        // No data loaders with more FIs
        //TODO skończyć ten test

        // Act and Assert
        balanceDataLoadersUseCase.balanceDataLoaders();
        for (DataLoaderModel dataLoader : dataLoadersWithLessFIsThanRecommended) {
            assertTrue(dataLoader.getFinancialInstrumentModelCollection().size() <= dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader());
        }
        verify(dataLoaderRepository, atLeastOnce()).save(any(DataLoaderModel.class));
    }

    @Test
    void testInfiniteLoopPrevention() {
        // Arrange
        // No data loaders with more FIs

        // Act and Assert
        balanceDataLoadersUseCase.balanceDataLoaders();
        // Since there are no data loaders with more FIs, no infinite loop should occur
        verify(dataLoaderRepository, times(0)).save(any(DataLoaderModel.class));
    }
}
