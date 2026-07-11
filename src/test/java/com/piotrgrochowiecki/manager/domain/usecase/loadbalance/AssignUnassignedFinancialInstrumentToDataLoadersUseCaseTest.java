package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import com.piotrgrochowiecki.manager.domain.service.FinancialInstrumentService;
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
@DisplayName("AssignUnassignedFinancialInstrumentToDataLoadersUseCase")
class AssignUnassignedFinancialInstrumentToDataLoadersUseCaseTest {

    @Mock
    private FinancialInstrumentService financialInstrumentService;

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private FinancialInstrumentRepository financialInstrumentRepository;

    @InjectMocks
    private AssignUnassignedFinancialInstrumentToDataLoadersUseCase useCase;

    @Test
    @DisplayName("""
            Given all financial instruments are assigned,
            when assignUnassignedInstrumentsToActiveDataLoaders is called,
            then should return early without performing any assignments,""")
    void should_returnEarly() {
        // Given
        when(financialInstrumentRepository.existsWithNoDataLoaderAssigned()).thenReturn(false);

        // When
        useCase.assignUnassignedInstrumentsToActiveDataLoaders();

        // Then
        verify(financialInstrumentRepository).existsWithNoDataLoaderAssigned();
        verify(financialInstrumentRepository, never()).findUnassignedToAnyDataLoader();
        verify(dataLoaderRepository, never()).findActiveDataLoadersWithTooLowOrNullLoadStatus();
        verify(financialInstrumentService, never()).update(any());
    }

    @Test
    @DisplayName("""
            Given unassigned financial instruments and active data loaders with capacity,
            when assignUnassignedInstrumentsToActiveDataLoaders is called,
            then should distribute financial instruments using round-robin algorithm,""")
    void should_distributeFinancialInstrumentsUsingRoundRobin() {
        // Given
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(10L, "Apple Inc.", "AAPL", null),
                buildFinancialInstrument(11L, "Microsoft Inc.", "MSFT", null),
                buildFinancialInstrument(12L, "Google Inc.", "GOOGL", null),
                buildFinancialInstrument(13L, "Tesla Inc.", "TSLA", null),
                buildFinancialInstrument(14L, "Amazon Inc.", "AMZN", null),
                buildFinancialInstrument(15L, "Meta Inc.", "META", null)
        ));

        List<DataLoaderModel> activeDataLoaders = new LinkedList<>(List.of(
                buildDataLoader(3L, DataLoaderModel.Status.TOO_LOW),
                buildDataLoader(5L, DataLoaderModel.Status.TOO_LOW),
                buildDataLoader(7L, DataLoaderModel.Status.TOO_LOW)
        ));

        when(financialInstrumentRepository.existsWithNoDataLoaderAssigned()).thenReturn(true);
        when(financialInstrumentRepository.findUnassignedToAnyDataLoader()).thenReturn(unassignedFIs);
        when(dataLoaderRepository.findActiveDataLoadersWithTooLowOrNullLoadStatus()).thenReturn(activeDataLoaders);

        // When
        useCase.assignUnassignedInstrumentsToActiveDataLoaders();

        // Then
        ArgumentCaptor<FinancialInstrumentModel> captor = ArgumentCaptor.forClass(FinancialInstrumentModel.class);
        verify(financialInstrumentService, times(6)).update(captor.capture());

        List<FinancialInstrumentModel> capturedUpdates = captor.getAllValues();
        assertThat(capturedUpdates.get(0).getDataLoaderId()).isEqualTo(3L); // FI[0] % 3 = 0 -> DL_3
        assertThat(capturedUpdates.get(1).getDataLoaderId()).isEqualTo(5L); // FI[1] % 3 = 1 -> DL_5
        assertThat(capturedUpdates.get(2).getDataLoaderId()).isEqualTo(7L); // FI[2] % 3 = 2 -> DL_7
        assertThat(capturedUpdates.get(3).getDataLoaderId()).isEqualTo(3L); // FI[3] % 3 = 0 -> DL_3
        assertThat(capturedUpdates.get(4).getDataLoaderId()).isEqualTo(5L); // FI[4] % 3 = 1 -> DL_5
        assertThat(capturedUpdates.get(5).getDataLoaderId()).isEqualTo(7L); // FI[5] % 3 = 2 -> DL_7
    }

    @Test
    @DisplayName("""
            Given unassigned financial instruments but no active data loaders with capacity,
            when assignUnassignedInstrumentsToActiveDataLoaders is called,
            then should not perform any assignments,""")
    void should_notAssignWhenNoAvailableDataLoaders() {
        // Given
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(20L, "Microsoft Inc.", "MSFT", null),
                buildFinancialInstrument(21L, "Apple Inc.", "AAPL", null)
        ));

        when(financialInstrumentRepository.existsWithNoDataLoaderAssigned()).thenReturn(true);
        when(financialInstrumentRepository.findUnassignedToAnyDataLoader()).thenReturn(unassignedFIs);
        when(dataLoaderRepository.findActiveDataLoadersWithTooLowOrNullLoadStatus()).thenReturn(new LinkedList<>());

        // When
        useCase.assignUnassignedInstrumentsToActiveDataLoaders();

        // Then
        verify(financialInstrumentService, never()).update(any());
    }

    @Test
    @DisplayName("""
            Given a single unassigned financial instrument and a single active data loader,
            when assignUnassignedInstrumentsToActiveDataLoaders is called,
            then should assign the financial instrument to the only available data loader,""")
    void should_assignSingleFinancialInstrumentToSingleDataLoader() {
        // Given
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(30L, "Microsoft Inc.", "MSFT", null)
        ));

        List<DataLoaderModel> activeDataLoaders = new LinkedList<>(List.of(
                buildDataLoader(8L, DataLoaderModel.Status.TOO_LOW)
        ));

        when(financialInstrumentRepository.existsWithNoDataLoaderAssigned()).thenReturn(true);
        when(financialInstrumentRepository.findUnassignedToAnyDataLoader()).thenReturn(unassignedFIs);
        when(dataLoaderRepository.findActiveDataLoadersWithTooLowOrNullLoadStatus()).thenReturn(activeDataLoaders);

        // When
        useCase.assignUnassignedInstrumentsToActiveDataLoaders();

        // Then
        ArgumentCaptor<FinancialInstrumentModel> captor = ArgumentCaptor.forClass(FinancialInstrumentModel.class);
        verify(financialInstrumentService).update(captor.capture());

        FinancialInstrumentModel capturedUpdate = captor.getValue();
        assertThat(capturedUpdate.getDataLoaderId()).isEqualTo(8L);
    }

    @Test
    @DisplayName("""
            Given more unassigned financial instruments than available data loaders,
            when assignUnassignedInstrumentsToActiveDataLoaders is called,
            then should distribute financial instruments with round-robin wrapping,""")
    void should_wrapRoundRobinWhenMoreFIsThanDataLoaders() {
        // Given
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(40L, "Microsoft Inc.", "MSFT", null),
                buildFinancialInstrument(41L, "Apple Inc.", "AAPL", null),
                buildFinancialInstrument(42L, "Google Inc.", "GOOGL", null),
                buildFinancialInstrument(43L, "Tesla Inc.", "TSLA", null),
                buildFinancialInstrument(44L, "Amazon Inc.", "AMZN", null)
        ));

        List<DataLoaderModel> activeDataLoaders = new LinkedList<>(List.of(
                buildDataLoader(9L, DataLoaderModel.Status.TOO_LOW),
                buildDataLoader(10L, DataLoaderModel.Status.TOO_LOW)
        ));

        when(financialInstrumentRepository.existsWithNoDataLoaderAssigned()).thenReturn(true);
        when(financialInstrumentRepository.findUnassignedToAnyDataLoader()).thenReturn(unassignedFIs);
        when(dataLoaderRepository.findActiveDataLoadersWithTooLowOrNullLoadStatus()).thenReturn(activeDataLoaders);

        // When
        useCase.assignUnassignedInstrumentsToActiveDataLoaders();

        // Then
        ArgumentCaptor<FinancialInstrumentModel> captor = ArgumentCaptor.forClass(FinancialInstrumentModel.class);
        verify(financialInstrumentService, times(5)).update(captor.capture());

        List<FinancialInstrumentModel> capturedUpdates = captor.getAllValues();
        assertThat(capturedUpdates.get(0).getDataLoaderId()).isEqualTo(9L);  // FI[0] % 2 = 0 -> DL_9
        assertThat(capturedUpdates.get(1).getDataLoaderId()).isEqualTo(10L); // FI[1] % 2 = 1 -> DL_10
        assertThat(capturedUpdates.get(2).getDataLoaderId()).isEqualTo(9L);  // FI[2] % 2 = 0 -> DL_9
        assertThat(capturedUpdates.get(3).getDataLoaderId()).isEqualTo(10L); // FI[3] % 2 = 1 -> DL_10
        assertThat(capturedUpdates.get(4).getDataLoaderId()).isEqualTo(9L);  // FI[4] % 2 = 0 -> DL_9
    }

    @Test
    @DisplayName("""
            Given unassigned financial instruments and data loaders with NULL status,
            when assignUnassignedInstrumentsToActiveDataLoaders is called,
            then should assign financial instruments to data loaders with NULL status,""")
    void should_assignToDataLoadersWithNullStatus() {
        // Given
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(50L, "Microsoft Inc.", "MSFT", null),
                buildFinancialInstrument(51L, "Apple Inc.", "AAPL", null)
        ));

        List<DataLoaderModel> activeDataLoaders = new LinkedList<>(List.of(
                buildDataLoader(11L, null), // NULL status
                buildDataLoader(12L, null)  // NULL status
        ));

        when(financialInstrumentRepository.existsWithNoDataLoaderAssigned()).thenReturn(true);
        when(financialInstrumentRepository.findUnassignedToAnyDataLoader()).thenReturn(unassignedFIs);
        when(dataLoaderRepository.findActiveDataLoadersWithTooLowOrNullLoadStatus()).thenReturn(activeDataLoaders);

        // When
        useCase.assignUnassignedInstrumentsToActiveDataLoaders();

        // Then
        ArgumentCaptor<FinancialInstrumentModel> captor = ArgumentCaptor.forClass(FinancialInstrumentModel.class);
        verify(financialInstrumentService, times(2)).update(captor.capture());

        List<FinancialInstrumentModel> capturedUpdates = captor.getAllValues();
        assertThat(capturedUpdates.get(0).getDataLoaderId()).isEqualTo(11L);
        assertThat(capturedUpdates.get(1).getDataLoaderId()).isEqualTo(12L);
    }

    @Test
    @DisplayName("""
            Given a mix of unassigned financial instruments,
            when assignUnassignedInstrumentsToActiveDataLoaders is called,
            then should preserve the original financial instrument data except dataLoaderId,""")
    void should_preserveFinancialInstrumentData() {
        // Given
        List<FinancialInstrumentModel> unassignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(60L, "Microsoft Inc.", "MSFT", null)
        ));

        List<DataLoaderModel> activeDataLoaders = new LinkedList<>(List.of(
                buildDataLoader(13L, DataLoaderModel.Status.TOO_LOW)
        ));

        when(financialInstrumentRepository.existsWithNoDataLoaderAssigned()).thenReturn(true);
        when(financialInstrumentRepository.findUnassignedToAnyDataLoader()).thenReturn(unassignedFIs);
        when(dataLoaderRepository.findActiveDataLoadersWithTooLowOrNullLoadStatus()).thenReturn(activeDataLoaders);

        // When
        useCase.assignUnassignedInstrumentsToActiveDataLoaders();

        // Then
        ArgumentCaptor<FinancialInstrumentModel> captor = ArgumentCaptor.forClass(FinancialInstrumentModel.class);
        verify(financialInstrumentService).update(captor.capture());

        FinancialInstrumentModel capturedUpdate = captor.getValue();
        assertThat(capturedUpdate.getId()).isEqualTo(60L);
        assertThat(capturedUpdate.getName()).isEqualTo("Microsoft Inc.");
        assertThat(capturedUpdate.getSymbol()).isEqualTo("MSFT");
        assertThat(capturedUpdate.getDataLoaderId()).isEqualTo(13L);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private FinancialInstrumentModel buildFinancialInstrument(Long id, String name, String symbol, Long dataLoaderId) {
        return FinancialInstrumentModel.builder()
                .id(id)
                .name(name)
                .symbol(symbol)
                .dataLoaderId(dataLoaderId)
                .build();
    }

    private DataLoaderModel buildDataLoader(Long id, DataLoaderModel.Status status) {
        return DataLoaderModel.builder()
                .id(id)
                .uuid(UUID.randomUUID().toString())
                .active(true)
                .lastConnectedOn(Instant.now())
                .lastInstantOfFinancialInstrumentsAssignment(Instant.now())
                .lastLoadStatusUpdate(Instant.now())
                .loadStatus(status)
                .build();
    }
}

