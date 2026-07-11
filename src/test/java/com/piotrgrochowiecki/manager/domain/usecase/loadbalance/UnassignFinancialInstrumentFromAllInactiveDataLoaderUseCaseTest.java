package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnassignFinancialInstrumentFromAllInactiveDataLoaderUseCase")
class UnassignFinancialInstrumentFromAllInactiveDataLoaderUseCaseTest {

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private FinancialInstrumentRepository financialInstrumentRepository;

    @InjectMocks
    private UnassignFinancialInstrumentFromAllInactiveDataLoaderUseCase useCase;

    @Test
    @DisplayName("""
            Given no inactive data loaders exist,
            when unassignFinancialInstrumentFromInactiveDataLoader is called,
            then should not interact with financialInstrumentRepository or save any data loader,""")
    void should_handleNoInactiveDataLoaders() {
        // Given
        when(dataLoaderRepository.findInactiveDataLoaders()).thenReturn(new ArrayList<>());

        // When
        useCase.unassignFinancialInstrumentFromInactiveDataLoader();

        // Then
        verify(dataLoaderRepository).findInactiveDataLoaders();
        verify(financialInstrumentRepository, never()).findByDataLoaderId(any());
        verify(financialInstrumentRepository, never()).detachDataLoaderBasedOnIds(any());
        verify(dataLoaderRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            Given a single inactive data loader with multiple assigned financial instruments,
            when unassignFinancialInstrumentFromInactiveDataLoader is called,
            then should find assigned financial instruments, unassign them in batch, and update the loader,""")
    void should_unassignFinancialInstrumentsFromSingleInactiveDataLoader() {
        // Given
        Instant beforeCall = Instant.now();

        DataLoaderModel inactiveDataLoader = buildDataLoader(3L, DataLoaderModel.Status.BALANCED);
        Collection<DataLoaderModel> inactiveDataLoaders = new LinkedList<>(List.of(inactiveDataLoader));

        List<FinancialInstrumentModel> assignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(100L, "Microsoft Inc.", "MSFT", 3L),
                buildFinancialInstrument(101L, "Apple Inc.", "AAPL", 3L),
                buildFinancialInstrument(102L, "Google Inc.", "GOOGL", 3L)
        ));

        when(dataLoaderRepository.findInactiveDataLoaders()).thenReturn(inactiveDataLoaders);
        when(financialInstrumentRepository.findByDataLoaderId(3L)).thenReturn(assignedFIs);
        when(financialInstrumentRepository.detachDataLoaderBasedOnIds(any())).thenReturn(3);

        // When
        useCase.unassignFinancialInstrumentFromInactiveDataLoader();

        Instant afterCall = Instant.now();

        // Then
        verify(financialInstrumentRepository).findByDataLoaderId(3L);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(financialInstrumentRepository).detachDataLoaderBasedOnIds(idsCaptor.capture());

        List<Long> capturedIds = idsCaptor.getValue();
        assertThat(capturedIds).containsExactly(100L, 101L, 102L);

        ArgumentCaptor<DataLoaderModel> dataLoaderCaptor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderRepository).save(dataLoaderCaptor.capture());

        DataLoaderModel capturedDataLoader = dataLoaderCaptor.getValue();
        assertThat(capturedDataLoader.getId()).isEqualTo(3L);
        assertThat(capturedDataLoader.getLastLoadStatusUpdate())
                .isNotNull()
                .isAfterOrEqualTo(beforeCall)
                .isBeforeOrEqualTo(afterCall);
    }

    @Test
    @DisplayName("""
            Given a single inactive data loader with no assigned financial instruments,
            when unassignFinancialInstrumentFromInactiveDataLoader is called,
            then should skip unassign batch operation but still update and save the loader,""")
    void should_skipUnassignWhenNoFinancialInstrumentsAssigned() {
        // Given
        Instant beforeCall = Instant.now();

        DataLoaderModel inactiveDataLoader = buildDataLoader(5L, DataLoaderModel.Status.TOO_HIGH);
        Collection<DataLoaderModel> inactiveDataLoaders = new LinkedList<>(List.of(inactiveDataLoader));

        when(dataLoaderRepository.findInactiveDataLoaders()).thenReturn(inactiveDataLoaders);
        when(financialInstrumentRepository.findByDataLoaderId(5L)).thenReturn(new ArrayList<>());

        // When
        useCase.unassignFinancialInstrumentFromInactiveDataLoader();

        Instant afterCall = Instant.now();

        // Then
        verify(financialInstrumentRepository).findByDataLoaderId(5L);
        verify(financialInstrumentRepository, never()).detachDataLoaderBasedOnIds(any());

        ArgumentCaptor<DataLoaderModel> dataLoaderCaptor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderRepository).save(dataLoaderCaptor.capture());

        DataLoaderModel capturedDataLoader = dataLoaderCaptor.getValue();
        assertThat(capturedDataLoader.getId()).isEqualTo(5L);
        assertThat(capturedDataLoader.getLastLoadStatusUpdate())
                .isNotNull()
                .isAfterOrEqualTo(beforeCall)
                .isBeforeOrEqualTo(afterCall);
    }

    @Test
    @DisplayName("""
            Given multiple inactive data loaders where one has assigned instruments and one does not,
            when unassignFinancialInstrumentFromInactiveDataLoader is called,
            then should process each loader independently and only detach for loaders with assigned instruments,""")
    void should_processMultipleInactiveDataLoadersIndependently() {
        // Given
        Instant beforeCall = Instant.now();

        DataLoaderModel loader1 = buildDataLoader(7L, DataLoaderModel.Status.TOO_LOW);
        DataLoaderModel loader2 = buildDataLoader(9L, DataLoaderModel.Status.BALANCED);
        Collection<DataLoaderModel> inactiveDataLoaders = new LinkedList<>(List.of(loader1, loader2));

        List<FinancialInstrumentModel> loader1FIs = new LinkedList<>(List.of(
                buildFinancialInstrument(200L, "Tesla Inc.", "TSLA", 7L),
                buildFinancialInstrument(201L, "Meta Inc.", "META", 7L)
        ));

        when(dataLoaderRepository.findInactiveDataLoaders()).thenReturn(inactiveDataLoaders);
        when(financialInstrumentRepository.findByDataLoaderId(7L)).thenReturn(loader1FIs);
        when(financialInstrumentRepository.findByDataLoaderId(9L)).thenReturn(new ArrayList<>());
        when(financialInstrumentRepository.detachDataLoaderBasedOnIds(any())).thenReturn(2);

        // When
        useCase.unassignFinancialInstrumentFromInactiveDataLoader();

        Instant afterCall = Instant.now();

        // Then
        verify(financialInstrumentRepository).findByDataLoaderId(7L);
        verify(financialInstrumentRepository).findByDataLoaderId(9L);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(financialInstrumentRepository, times(1)).detachDataLoaderBasedOnIds(idsCaptor.capture());

        List<Long> capturedIds = idsCaptor.getValue();
        assertThat(capturedIds).containsExactly(200L, 201L);

        ArgumentCaptor<DataLoaderModel> dataLoaderCaptor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderRepository, times(2)).save(dataLoaderCaptor.capture());

        List<DataLoaderModel> capturedLoaders = dataLoaderCaptor.getAllValues();
        assertThat(capturedLoaders).hasSize(2);
        assertThat(capturedLoaders.get(0).getId()).isEqualTo(7L);
        assertThat(capturedLoaders.get(1).getId()).isEqualTo(9L);

        capturedLoaders.forEach(loader ->
                assertThat(loader.getLastLoadStatusUpdate())
                        .isNotNull()
                        .isAfterOrEqualTo(beforeCall)
                        .isBeforeOrEqualTo(afterCall)
        );
    }

    @Test
    @DisplayName("""
            Given a single inactive data loader with a single assigned financial instrument,
            when unassignFinancialInstrumentFromInactiveDataLoader is called,
            then should pass a singleton list with the instrument id to the detach operation,""")
    void should_handleSingleAssignedFinancialInstrument() {
        // Given
        DataLoaderModel inactiveDataLoader = buildDataLoader(11L, DataLoaderModel.Status.TOO_LOW);
        Collection<DataLoaderModel> inactiveDataLoaders = new LinkedList<>(List.of(inactiveDataLoader));

        List<FinancialInstrumentModel> assignedFI = new LinkedList<>(List.of(
                buildFinancialInstrument(300L, "Amazon Inc.", "AMZN", 11L)
        ));

        when(dataLoaderRepository.findInactiveDataLoaders()).thenReturn(inactiveDataLoaders);
        when(financialInstrumentRepository.findByDataLoaderId(11L)).thenReturn(assignedFI);
        when(financialInstrumentRepository.detachDataLoaderBasedOnIds(any())).thenReturn(1);

        // When
        useCase.unassignFinancialInstrumentFromInactiveDataLoader();

        // Then
        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(financialInstrumentRepository).detachDataLoaderBasedOnIds(idsCaptor.capture());

        List<Long> capturedIds = idsCaptor.getValue();
        assertThat(capturedIds).hasSize(1).containsExactly(300L);

        verify(dataLoaderRepository).save(any());
    }

    @Test
    @DisplayName("""
            Given multiple inactive data loaders all with assigned financial instruments,
            when unassignFinancialInstrumentFromInactiveDataLoader is called,
            then should process each loader and collect ids correctly without mixing assignments,""")
    void should_keepFinancialInstrumentAssignmentsIsolatedPerDataLoader() {
        // Given
        Instant beforeCall = Instant.now();

        DataLoaderModel loader1 = buildDataLoader(13L, DataLoaderModel.Status.BALANCED);
        DataLoaderModel loader2 = buildDataLoader(15L, DataLoaderModel.Status.TOO_HIGH);
        DataLoaderModel loader3 = buildDataLoader(17L, DataLoaderModel.Status.TOO_LOW);
        Collection<DataLoaderModel> inactiveDataLoaders = new LinkedList<>(List.of(loader1, loader2, loader3));

        List<FinancialInstrumentModel> loader1FIs = new LinkedList<>(List.of(
                buildFinancialInstrument(400L, "Intel Corp.", "INTC", 13L),
                buildFinancialInstrument(401L, "Nvidia Corp.", "NVDA", 13L)
        ));

        List<FinancialInstrumentModel> loader2FIs = new LinkedList<>(List.of(
                buildFinancialInstrument(402L, "AMD Inc.", "AMD", 15L)
        ));

        List<FinancialInstrumentModel> loader3FIs = new LinkedList<>(List.of(
                buildFinancialInstrument(403L, "Broadcom Inc.", "AVGO", 17L),
                buildFinancialInstrument(404L, "QUALCOMM Inc.", "QCOM", 17L),
                buildFinancialInstrument(405L, "Marvell Technology", "MRVL", 17L)
        ));

        when(dataLoaderRepository.findInactiveDataLoaders()).thenReturn(inactiveDataLoaders);
        when(financialInstrumentRepository.findByDataLoaderId(13L)).thenReturn(loader1FIs);
        when(financialInstrumentRepository.findByDataLoaderId(15L)).thenReturn(loader2FIs);
        when(financialInstrumentRepository.findByDataLoaderId(17L)).thenReturn(loader3FIs);
        when(financialInstrumentRepository.detachDataLoaderBasedOnIds(any())).thenReturn(2, 1, 3);

        // When
        useCase.unassignFinancialInstrumentFromInactiveDataLoader();

        Instant afterCall = Instant.now();

        // Then
        verify(financialInstrumentRepository).findByDataLoaderId(13L);
        verify(financialInstrumentRepository).findByDataLoaderId(15L);
        verify(financialInstrumentRepository).findByDataLoaderId(17L);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(financialInstrumentRepository, times(3)).detachDataLoaderBasedOnIds(idsCaptor.capture());

        List<List<Long>> allCapturedIds = idsCaptor.getAllValues();
        assertThat(allCapturedIds.get(0)).containsExactly(400L, 401L);
        assertThat(allCapturedIds.get(1)).containsExactly(402L);
        assertThat(allCapturedIds.get(2)).containsExactly(403L, 404L, 405L);

        ArgumentCaptor<DataLoaderModel> dataLoaderCaptor = ArgumentCaptor.forClass(DataLoaderModel.class);
        verify(dataLoaderRepository, times(3)).save(dataLoaderCaptor.capture());

        List<DataLoaderModel> capturedLoaders = dataLoaderCaptor.getAllValues();
        assertThat(capturedLoaders).hasSize(3);
        assertThat(capturedLoaders.get(0).getId()).isEqualTo(13L);
        assertThat(capturedLoaders.get(1).getId()).isEqualTo(15L);
        assertThat(capturedLoaders.get(2).getId()).isEqualTo(17L);

        capturedLoaders.forEach(loader ->
                assertThat(loader.getLastLoadStatusUpdate())
                        .isNotNull()
                        .isAfterOrEqualTo(beforeCall)
                        .isBeforeOrEqualTo(afterCall)
        );
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
                .active(false)
                .lastConnectedOn(Instant.now())
                .lastInstantOfFinancialInstrumentsAssignment(Instant.now())
                .lastLoadStatusUpdate(Instant.now())
                .loadStatus(status)
                .build();
    }
}
