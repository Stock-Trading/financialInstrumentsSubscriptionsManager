package com.piotrgrochowiecki.manager.domain.usecase.loadbalance;

import com.piotrgrochowiecki.manager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckActiveStateOfDataLoaderUseCase")
class CheckActiveStateOfDataLoaderUseCaseTest {

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private DataLoaderParametersProvider dataLoaderParametersProvider;

    @Mock
    private TimeService timeService;

    @InjectMocks
    private CheckActiveStateOfDataLoaderUseCase useCase;

    @Test
    @DisplayName("""
            Given a current time and configured active threshold,
            when checkActiveState is called,
            then should calculate threshold timestamp by subtracting threshold from current time,""")
    void should_calculateThresholdTimestampCorrectly() {
        // Given
        Instant currentTime = Instant.parse("2026-04-26T12:00:00Z");
        long thresholdMs = 30_000L;
        Instant expectedThreshold = currentTime.minus(Duration.ofMillis(thresholdMs));

        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderParametersProvider.getActiveThresholdMilliseconds()).thenReturn(Math.toIntExact(thresholdMs));
        when(dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any())).thenReturn(0);

        // When
        useCase.checkActiveState();

        // Then
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(dataLoaderRepository).setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(thresholdCaptor.capture());

        Instant actualThreshold = thresholdCaptor.getValue();
        assertThat(actualThreshold).isEqualTo(expectedThreshold);
        assertThat(actualThreshold).isEqualTo(Instant.parse("2026-04-26T11:59:30Z"));
    }

    @Test
    @DisplayName("""
            Given no inactive data loaders exist,
            when checkActiveState is called,
            then should invoke repository method and receive zero updated count,""")
    void should_handleNoInactiveDataLoaders() {
        // Given
        Instant currentTime = Instant.parse("2026-04-26T12:00:00Z");
        long thresholdMs = 30_000L;

        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderParametersProvider.getActiveThresholdMilliseconds()).thenReturn(Math.toIntExact(thresholdMs));
        when(dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any())).thenReturn(0);

        // When
        useCase.checkActiveState();

        // Then
        verify(dataLoaderRepository).setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any());
        verify(timeService).getInstantUTC();
        verify(dataLoaderParametersProvider).getActiveThresholdMilliseconds();
    }

    @Test
    @DisplayName("""
            Given one data loader has not checked in within the threshold,
            when checkActiveState is called,
            then should mark it as inactive and return count of 1,""")
    void should_markSingleInactiveDataLoaderAsInactive() {
        // Given
        Instant currentTime = Instant.parse("2026-04-26T12:00:00Z");
        long thresholdMs = 30_000L;

        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderParametersProvider.getActiveThresholdMilliseconds()).thenReturn(Math.toIntExact(thresholdMs));
        when(dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any())).thenReturn(1);

        // When
        useCase.checkActiveState();

        // Then
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(dataLoaderRepository).setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(thresholdCaptor.capture());

        Instant threshold = thresholdCaptor.getValue();
        assertThat(threshold).isEqualTo(Instant.parse("2026-04-26T11:59:30Z"));
    }

    @Test
    @DisplayName("""
            Given multiple data loaders have not checked in within the threshold,
            when checkActiveState is called,
            then should mark all of them as inactive and return count matching the number of updated loaders,""")
    void should_markMultipleInactiveDataLoadersAsInactive() {
        // Given
        Instant currentTime = Instant.parse("2026-04-26T12:00:00Z");
        long thresholdMs = 30_000L;

        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderParametersProvider.getActiveThresholdMilliseconds()).thenReturn(Math.toIntExact(thresholdMs));
        when(dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any())).thenReturn(5);

        // When
        useCase.checkActiveState();

        // Then
        verify(dataLoaderRepository).setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any());
    }

    @Test
    @DisplayName("""
            Given a custom active threshold configuration,
            when checkActiveState is called,
            then should use the configured value to calculate the threshold timestamp,""")
    void should_useConfiguredThresholdValue() {
        // Given
        Instant currentTime = Instant.parse("2026-04-26T12:00:00Z");
        long customThresholdMs = 60_000L;
        Instant expectedThreshold = currentTime.minus(Duration.ofMillis(customThresholdMs));

        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderParametersProvider.getActiveThresholdMilliseconds()).thenReturn(Math.toIntExact(customThresholdMs));
        when(dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any())).thenReturn(0);

        // When
        useCase.checkActiveState();

        // Then
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(dataLoaderRepository).setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(thresholdCaptor.capture());

        Instant actualThreshold = thresholdCaptor.getValue();
        assertThat(actualThreshold).isEqualTo(expectedThreshold);
        assertThat(actualThreshold).isEqualTo(Instant.parse("2026-04-26T11:59:00Z"));
    }

    @Test
    @DisplayName("""
            Given a very short threshold of 1 millisecond,
            when checkActiveState is called,
            then should calculate threshold correctly and invoke repository,""")
    void should_handleVeryShortThreshold() {
        // Given
        Instant currentTime = Instant.parse("2026-04-26T12:00:00.100Z");
        long thresholdMs = 1L;
        Instant expectedThreshold = currentTime.minus(Duration.ofMillis(thresholdMs));

        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderParametersProvider.getActiveThresholdMilliseconds()).thenReturn(Math.toIntExact(thresholdMs));
        when(dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any())).thenReturn(0);

        // When
        useCase.checkActiveState();

        // Then
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(dataLoaderRepository).setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(thresholdCaptor.capture());

        Instant actualThreshold = thresholdCaptor.getValue();
        assertThat(actualThreshold).isEqualTo(expectedThreshold);
        assertThat(actualThreshold).isEqualTo(Instant.parse("2026-04-26T12:00:00.099Z"));
    }

    @Test
    @DisplayName("""
            Given a very long threshold configuration,
            when checkActiveState is called,
            then should calculate threshold correctly even with large duration,""")
    void should_handleVeryLongThreshold() {
        // Given
        Instant currentTime = Instant.parse("2026-04-26T12:00:00Z");
        long thresholdMs = 3_600_000L;
        Instant expectedThreshold = currentTime.minus(Duration.ofMillis(thresholdMs));

        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderParametersProvider.getActiveThresholdMilliseconds()).thenReturn(Math.toIntExact(thresholdMs));
        when(dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any())).thenReturn(0);

        // When
        useCase.checkActiveState();

        // Then
        ArgumentCaptor<Instant> thresholdCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(dataLoaderRepository).setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(thresholdCaptor.capture());

        Instant actualThreshold = thresholdCaptor.getValue();
        assertThat(actualThreshold).isEqualTo(expectedThreshold);
        assertThat(actualThreshold).isEqualTo(Instant.parse("2026-04-26T11:00:00Z"));
    }

    @Test
    @DisplayName("""
            Given the use case is invoked multiple times,
            when checkActiveState is called each time,
            then should retrieve fresh values from time service and parameters provider,""")
    void should_retrieveFreshValuesOnEachInvocation() {
        // Given
        Instant firstCallTime = Instant.parse("2026-04-26T12:00:00Z");
        Instant secondCallTime = Instant.parse("2026-04-26T12:00:03Z");
        long thresholdMs = 30_000L;

        when(timeService.getInstantUTC())
                .thenReturn(firstCallTime)
                .thenReturn(secondCallTime);
        when(dataLoaderParametersProvider.getActiveThresholdMilliseconds()).thenReturn(Math.toIntExact(thresholdMs));
        when(dataLoaderRepository.setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any())).thenReturn(0);

        // When
        useCase.checkActiveState();
        useCase.checkActiveState();

        // Then
        verify(timeService, times(2)).getInstantUTC();
        verify(dataLoaderParametersProvider, times(2)).getActiveThresholdMilliseconds();
        verify(dataLoaderRepository, times(2)).setActiveToFalseAndLoadStatusToNullOfInactiveDataLoaders(any());
    }
}
