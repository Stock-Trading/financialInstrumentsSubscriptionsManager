package com.piotrgrochowiecki.manager.domain.usecase;

import static org.junit.jupiter.api.Assertions.*;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.ports.FinancialInstrumentRepository;
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
    private FinancialInstrumentRepository financialInstrumentRepository;

    @Mock
    private DataLoaderParametersProvider dataLoaderParametersProvider;

    @InjectMocks
    private BalanceDataLoadersUseCase balanceDataLoadersUseCase;

    private List<DataLoaderModel> dataLoadersWithMoreFIsThanRecommended;
    private List<DataLoaderModel> dataLoadersWithLessFIsThanRecommended;

    @Test
    @DisplayName("""
            Given one DL with 2 FIs and one DL with 6 FIs,
            when balanceDataLoaders() is invoked,
            then 6th FIs should be reassigned to first DL.
            """)
    void testSuccessfulBalancing() {
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
        verify(dataLoaderRepository, atLeastOnce()).save(any(DataLoaderModel.class));
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
        dataLoadersWithMoreFIsThanRecommended.clear(); // No data loaders with more FIs

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
        dataLoadersWithMoreFIsThanRecommended.clear(); // No data loaders with more FIs

        // Act and Assert
        balanceDataLoadersUseCase.balanceDataLoaders();
        // Since there are no data loaders with more FIs, no infinite loop should occur
        verify(dataLoaderRepository, atLeastOnce()).save(any(DataLoaderModel.class));
    }
}
