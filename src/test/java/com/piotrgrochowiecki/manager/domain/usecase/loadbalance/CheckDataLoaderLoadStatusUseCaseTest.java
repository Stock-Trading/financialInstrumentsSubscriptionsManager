package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import com.piotrgrochowiecki.manager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckDataLoaderLoadStatusUseCase")
class CheckDataLoaderLoadStatusUseCaseTest {

    @Mock
    private DataLoaderParametersProvider dataLoaderParametersProvider;

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private FinancialInstrumentRepository financialInstrumentRepository;

    @Mock
    private DataLoaderService dataLoaderService;

    @Mock
    private TimeService timeService;

    @InjectMocks
    private CheckDataLoaderLoadStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(5);
        useCase.initialize();
    }

    @Test
    @DisplayName("""
            Given no active data loaders,
            when checkLoadStatus is called,
            then should not query financial instruments nor update any data loader,""")
    void should_returnEarlyWhenNoActiveDataLoaders() {
        // Given
        when(dataLoaderRepository.findActiveDataLoaders()).thenReturn(new LinkedList<>());

        // When
        useCase.checkLoadStatus();

        // Then
        verify(dataLoaderRepository).findActiveDataLoaders();
        verify(financialInstrumentRepository, never()).findNumberOfFinancialInstrumentsAssignedToDataLoader(any());
        verify(dataLoaderService, never()).update(any());
    }

    @Test
    @DisplayName("""
            Given an active data loader with assigned financial instruments count equal to the recommended number,
            when checkLoadStatus is called,
            then should set its load status to BALANCED,""")
    void should_setBalancedStatusWhenAssignedCountEqualsRecommended() {
        // Given
        DataLoaderModel dataLoader = buildDataLoader(3L);
        Instant now = Instant.parse("2024-01-01T10:00:00Z");

        when(dataLoaderRepository.findActiveDataLoaders()).thenReturn(new LinkedList<>(List.of(dataLoader)));
        when(financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(3L)).thenReturn(5L);
        when(timeService.getInstantUTC()).thenReturn(now);

        // When
        useCase.checkLoadStatus();

        // Then
        ArgumentCaptor<DataLoaderModel> captor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderService).update(captor.capture());

        DataLoaderModel updated = captor.getValue();
        assertThat(updated.getLoadStatus()).isEqualTo(DataLoaderModel.Status.BALANCED);
        assertThat(updated.getLastLoadStatusUpdate()).isEqualTo(now);
    }

    @Test
    @DisplayName("""
            Given an active data loader with assigned financial instruments count below the recommended number,
            when checkLoadStatus is called,
            then should set its load status to TOO_LOW,""")
    void should_setTooLowStatusWhenAssignedCountBelowRecommended() {
        // Given
        DataLoaderModel dataLoader = buildDataLoader(5L);

        when(dataLoaderRepository.findActiveDataLoaders()).thenReturn(new LinkedList<>(List.of(dataLoader)));
        when(financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(5L)).thenReturn(2L);
        when(timeService.getInstantUTC()).thenReturn(Instant.now());

        // When
        useCase.checkLoadStatus();

        // Then
        ArgumentCaptor<DataLoaderModel> captor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderService).update(captor.capture());

        DataLoaderModel updated = captor.getValue();
        assertThat(updated.getLoadStatus()).isEqualTo(DataLoaderModel.Status.TOO_LOW);
    }

    @Test
    @DisplayName("""
            Given an active data loader with assigned financial instruments count above the recommended number,
            when checkLoadStatus is called,
            then should set its load status to TOO_HIGH,""")
    void should_setTooHighStatusWhenAssignedCountAboveRecommended() {
        // Given
        DataLoaderModel dataLoader = buildDataLoader(7L);

        when(dataLoaderRepository.findActiveDataLoaders()).thenReturn(new LinkedList<>(List.of(dataLoader)));
        when(financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(7L)).thenReturn(9L);
        when(timeService.getInstantUTC()).thenReturn(Instant.now());

        // When
        useCase.checkLoadStatus();

        // Then
        ArgumentCaptor<DataLoaderModel> captor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderService).update(captor.capture());

        DataLoaderModel updated = captor.getValue();
        assertThat(updated.getLoadStatus()).isEqualTo(DataLoaderModel.Status.TOO_HIGH);
    }

    @Test
    @DisplayName("""
            Given multiple active data loaders with different assigned financial instruments counts,
            when checkLoadStatus is called,
            then should independently determine and persist the correct load status for each,""")
    void should_processMultipleDataLoadersIndependently() {
        // Given
        DataLoaderModel tooLowLoader = buildDataLoader(10L);
        DataLoaderModel balancedLoader = buildDataLoader(11L);
        DataLoaderModel tooHighLoader = buildDataLoader(12L);

        when(dataLoaderRepository.findActiveDataLoaders())
                .thenReturn(new LinkedList<>(List.of(tooLowLoader, balancedLoader, tooHighLoader)));
        when(financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(10L)).thenReturn(1L);
        when(financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(11L)).thenReturn(5L);
        when(financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(12L)).thenReturn(8L);
        when(timeService.getInstantUTC()).thenReturn(Instant.now());

        // When
        useCase.checkLoadStatus();

        // Then
        ArgumentCaptor<DataLoaderModel> captor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderService, times(3)).update(captor.capture());

        List<DataLoaderModel> updated = captor.getAllValues();
        assertThat(updated.get(0).getLoadStatus()).isEqualTo(DataLoaderModel.Status.TOO_LOW);
        assertThat(updated.get(1).getLoadStatus()).isEqualTo(DataLoaderModel.Status.BALANCED);
        assertThat(updated.get(2).getLoadStatus()).isEqualTo(DataLoaderModel.Status.TOO_HIGH);
    }

    @Test
    @DisplayName("""
            Given a mocked current instant returned by the time service,
            when checkLoadStatus is called,
            then should persist that instant as the data loader's lastLoadStatusUpdate,""")
    void should_updateLastLoadStatusUpdateTimestampUsingTimeService() {
        // Given
        DataLoaderModel dataLoader = buildDataLoader(13L);
        Instant mockedInstant = Instant.parse("2024-06-15T08:30:00Z");

        when(dataLoaderRepository.findActiveDataLoaders()).thenReturn(new LinkedList<>(List.of(dataLoader)));
        when(financialInstrumentRepository.findNumberOfFinancialInstrumentsAssignedToDataLoader(13L)).thenReturn(0L);
        when(timeService.getInstantUTC()).thenReturn(mockedInstant);

        // When
        useCase.checkLoadStatus();

        // Then
        ArgumentCaptor<DataLoaderModel> captor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderService).update(captor.capture());

        DataLoaderModel updated = captor.getValue();
        assertThat(updated.getLastLoadStatusUpdate()).isEqualTo(mockedInstant);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private DataLoaderModel buildDataLoader(Long id) {
        return DataLoaderModel.builder()
                .id(id)
                .uuid(UUID.randomUUID().toString())
                .active(true)
                .lastConnectedOn(Instant.now())
                .lastInstantOfFinancialInstrumentsAssignment(Instant.now())
                .lastLoadStatusUpdate(Instant.now())
                .loadStatus(null)
                .build();
    }
}
