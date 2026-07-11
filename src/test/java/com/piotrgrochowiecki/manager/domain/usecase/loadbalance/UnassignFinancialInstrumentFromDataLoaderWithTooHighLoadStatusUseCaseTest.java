package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnassignFinancialInstrumentFromDataLoaderWithTooHighLoadStatusUseCase")
class UnassignFinancialInstrumentFromDataLoaderWithTooHighLoadStatusUseCaseTest {

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private FinancialInstrumentRepository financialInstrumentRepository;

    @Mock
    private DataLoaderParametersProvider dataLoaderParametersProvider;

    @InjectMocks
    private UnassignFinancialInstrumentFromDataLoaderWithTooHighLoadStatusUseCase useCase;

    @Test
    @DisplayName("""
            Given no data loaders with too high load status and active flag set to true,
            when unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus is called,
            then should not interact with financialInstrumentRepository or dataLoaderParametersProvider,""")
    void should_handleNoOverloadedDataLoaders() {
        // Given
        when(dataLoaderRepository.findIdOfDataLoadersWithTooHighLoadStatusAndActiveFlagSetToTrue())
                .thenReturn(new ArrayList<>());

        // When
        useCase.unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus();

        // Then
        verify(dataLoaderRepository).findIdOfDataLoadersWithTooHighLoadStatusAndActiveFlagSetToTrue();
        verify(financialInstrumentRepository, never()).findByDataLoaderId(any());
        verify(financialInstrumentRepository, never()).detachDataLoaderBasedOnIds(any());
        verifyNoInteractions(dataLoaderParametersProvider);
    }

    @Test
    @DisplayName("""
            Given a single overloaded data loader with more assigned financial instruments than recommended,
            when unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus is called,
            then should keep the recommended number of oldest instruments and detach the excess ones,""")
    void should_unassignExcessFinancialInstrumentsFromSingleOverloadedDataLoader() {
        // Given
        List<Long> overloadedDataLoaderIds = new LinkedList<>(List.of(3L));

        List<FinancialInstrumentModel> assignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(100L, "Microsoft Inc.", "MSFT", 3L),
                buildFinancialInstrument(101L, "Apple Inc.", "AAPL", 3L),
                buildFinancialInstrument(102L, "Google Inc.", "GOOGL", 3L),
                buildFinancialInstrument(103L, "Tesla Inc.", "TSLA", 3L)
        ));

        when(dataLoaderRepository.findIdOfDataLoadersWithTooHighLoadStatusAndActiveFlagSetToTrue())
                .thenReturn(overloadedDataLoaderIds);
        when(financialInstrumentRepository.findByDataLoaderId(3L)).thenReturn(assignedFIs);
        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(2);
        when(financialInstrumentRepository.detachDataLoaderBasedOnIds(any())).thenReturn(2);

        // When
        useCase.unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus();

        // Then
        verify(financialInstrumentRepository).findByDataLoaderId(3L);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(financialInstrumentRepository).detachDataLoaderBasedOnIds(idsCaptor.capture());

        List<Long> capturedIds = idsCaptor.getValue();
        assertThat(capturedIds).containsExactly(102L, 103L);
    }

    @Test
    @DisplayName("""
            Given a single overloaded data loader with no financial instruments currently assigned,
            when unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus is called,
            then should call detach with an empty list of ids,""")
    void should_detachWithEmptyListWhenNoFinancialInstrumentsAssigned() {
        // Given
        List<Long> overloadedDataLoaderIds = new LinkedList<>(List.of(5L));

        when(dataLoaderRepository.findIdOfDataLoadersWithTooHighLoadStatusAndActiveFlagSetToTrue())
                .thenReturn(overloadedDataLoaderIds);
        when(financialInstrumentRepository.findByDataLoaderId(5L)).thenReturn(new ArrayList<>());
        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(5);
        when(financialInstrumentRepository.detachDataLoaderBasedOnIds(any())).thenReturn(0);

        // When
        useCase.unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus();

        // Then
        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(financialInstrumentRepository).detachDataLoaderBasedOnIds(idsCaptor.capture());

        List<Long> capturedIds = idsCaptor.getValue();
        assertThat(capturedIds).isEmpty();
    }

    @Test
    @DisplayName("""
            Given a single overloaded data loader with exactly the recommended number of financial instruments assigned,
            when unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus is called,
            then should detach with an empty list since there is no excess,""")
    void should_detachWithEmptyListWhenAssignedCountEqualsRecommended() {
        // Given
        List<Long> overloadedDataLoaderIds = new LinkedList<>(List.of(9L));

        List<FinancialInstrumentModel> assignedFIs = new LinkedList<>(List.of(
                buildFinancialInstrument(200L, "Amazon Inc.", "AMZN", 9L),
                buildFinancialInstrument(201L, "Meta Inc.", "META", 9L)
        ));

        when(dataLoaderRepository.findIdOfDataLoadersWithTooHighLoadStatusAndActiveFlagSetToTrue())
                .thenReturn(overloadedDataLoaderIds);
        when(financialInstrumentRepository.findByDataLoaderId(9L)).thenReturn(assignedFIs);
        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(2);
        when(financialInstrumentRepository.detachDataLoaderBasedOnIds(any())).thenReturn(0);

        // When
        useCase.unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus();

        // Then
        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(financialInstrumentRepository).detachDataLoaderBasedOnIds(idsCaptor.capture());

        List<Long> capturedIds = idsCaptor.getValue();
        assertThat(capturedIds).isEmpty();
    }

    @Test
    @DisplayName("""
            Given multiple overloaded data loaders each with excess financial instruments,
            when unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus is called,
            then should process each loader independently and detach the correct excess ids per loader,""")
    void should_processMultipleOverloadedDataLoadersIndependently() {
        // Given
        List<Long> overloadedDataLoaderIds = new LinkedList<>(List.of(13L, 15L));

        List<FinancialInstrumentModel> loader1FIs = new LinkedList<>(List.of(
                buildFinancialInstrument(300L, "Intel Corp.", "INTC", 13L),
                buildFinancialInstrument(301L, "Nvidia Corp.", "NVDA", 13L),
                buildFinancialInstrument(302L, "AMD Inc.", "AMD", 13L)
        ));

        List<FinancialInstrumentModel> loader2FIs = new LinkedList<>(List.of(
                buildFinancialInstrument(400L, "Broadcom Inc.", "AVGO", 15L),
                buildFinancialInstrument(401L, "QUALCOMM Inc.", "QCOM", 15L)
        ));

        when(dataLoaderRepository.findIdOfDataLoadersWithTooHighLoadStatusAndActiveFlagSetToTrue())
                .thenReturn(overloadedDataLoaderIds);
        when(financialInstrumentRepository.findByDataLoaderId(13L)).thenReturn(loader1FIs);
        when(financialInstrumentRepository.findByDataLoaderId(15L)).thenReturn(loader2FIs);
        when(dataLoaderParametersProvider.getRecommendedNumberOfFinancialInstrumentsPerDataLoader()).thenReturn(1);
        when(financialInstrumentRepository.detachDataLoaderBasedOnIds(any())).thenReturn(2, 1);

        // When
        useCase.unassignFinancialInstrumentsFromDataLoaderWithTooHighLoadStatus();

        // Then
        verify(financialInstrumentRepository).findByDataLoaderId(13L);
        verify(financialInstrumentRepository).findByDataLoaderId(15L);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(financialInstrumentRepository, times(2)).detachDataLoaderBasedOnIds(idsCaptor.capture());

        List<List<Long>> allCapturedIds = idsCaptor.getAllValues();
        assertThat(allCapturedIds.get(0)).containsExactly(301L, 302L);
        assertThat(allCapturedIds.get(1)).containsExactly(401L);
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

}
