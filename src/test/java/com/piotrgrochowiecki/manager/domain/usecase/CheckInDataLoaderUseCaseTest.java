package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.exception.NotFoundException;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInDataLoaderUseCaseTest {

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private DataLoaderService dataLoaderService;

    @Mock
    private TimeService timeService;

    @InjectMocks
    private CheckInDataLoaderUseCase checkInDataLoaderUseCase;

    @Test
    @DisplayName("""
            Given data loader exists in repository,
            when checking in data loader,
            then should update lastConnectedOn and set active to true
            """)
    void should_updateLastConnectedOnAndSetActiveTrueWhenCheckingIn() {
        String dataLoaderUuid = UUID.randomUUID().toString();
        Instant currentTime = Instant.now();
        DataLoaderModel dataLoaderModel = DataLoaderModel.builder()
                .id(3L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(Instant.now().minusSeconds(60))
                .active(false)
                .loadStatus(DataLoaderModel.Status.BALANCED)
                .lastLoadStatusUpdate(Instant.now())
                .build();
        DataLoaderModel updatedDataLoaderModel = DataLoaderModel.builder()
                .id(3L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(currentTime)
                .active(true)
                .loadStatus(DataLoaderModel.Status.BALANCED)
                .lastLoadStatusUpdate(Instant.now())
                .build();

        when(dataLoaderRepository.findByUuid(dataLoaderUuid)).thenReturn(dataLoaderModel);
        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderService.update(dataLoaderModel)).thenReturn(updatedDataLoaderModel);

        DataLoaderModel result = checkInDataLoaderUseCase.checkIn(dataLoaderUuid);

        assertThat(result).isNotNull();
        assertThat(result.getLastConnectedOn()).isEqualTo(currentTime);
        assertThat(result.getActive()).isTrue();
        verify(dataLoaderRepository).findByUuid(dataLoaderUuid);
        verify(timeService).getInstantUTC();
        verify(dataLoaderService).update(dataLoaderModel);
    }

    @Test
    @DisplayName("""
            Given data loader already active with recent check-in
            when checking in data loader again,
            then should update lastConnectedOn to new time and remain active
            """)
    void should_updateLastConnectedOnWhenCheckingInActiveDataLoader() {
        String dataLoaderUuid = UUID.randomUUID().toString();
        Instant previousCheckIn = Instant.now().minusSeconds(30);
        Instant newCheckIn = Instant.now();
        DataLoaderModel dataLoaderModel = DataLoaderModel.builder()
                .id(5L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(previousCheckIn)
                .active(true)
                .loadStatus(DataLoaderModel.Status.TOO_LOW)
                .lastLoadStatusUpdate(Instant.now())
                .build();
        DataLoaderModel updatedDataLoaderModel = DataLoaderModel.builder()
                .id(5L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(newCheckIn)
                .active(true)
                .loadStatus(DataLoaderModel.Status.TOO_LOW)
                .lastLoadStatusUpdate(Instant.now())
                .build();

        when(dataLoaderRepository.findByUuid(dataLoaderUuid)).thenReturn(dataLoaderModel);
        when(timeService.getInstantUTC()).thenReturn(newCheckIn);
        when(dataLoaderService.update(dataLoaderModel)).thenReturn(updatedDataLoaderModel);

        DataLoaderModel result = checkInDataLoaderUseCase.checkIn(dataLoaderUuid);

        assertThat(result).isNotNull();
        assertThat(result.getLastConnectedOn()).isEqualTo(newCheckIn);
        assertThat(result.getActive()).isTrue();
        verify(dataLoaderRepository).findByUuid(dataLoaderUuid);
        verify(timeService).getInstantUTC();
        verify(dataLoaderService).update(dataLoaderModel);
    }

    @Test
    @DisplayName("""
            Given data loader with different load status,
            when checking in data loader,
            then should preserve load status and lastLoadStatusUpdate
            """)
    void should_preserveLoadStatusWhenCheckingIn() {
        String dataLoaderUuid = UUID.randomUUID().toString();
        Instant currentTime = Instant.now();
        Instant loadStatusUpdateTime = Instant.now().minusSeconds(120);
        DataLoaderModel dataLoaderModel = DataLoaderModel.builder()
                .id(7L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(Instant.now().minusSeconds(60))
                .active(false)
                .loadStatus(DataLoaderModel.Status.TOO_HIGH)
                .lastLoadStatusUpdate(loadStatusUpdateTime)
                .build();
        DataLoaderModel updatedDataLoaderModel = DataLoaderModel.builder()
                .id(7L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(currentTime)
                .active(true)
                .loadStatus(DataLoaderModel.Status.TOO_HIGH)
                .lastLoadStatusUpdate(loadStatusUpdateTime)
                .build();

        when(dataLoaderRepository.findByUuid(dataLoaderUuid)).thenReturn(dataLoaderModel);
        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderService.update(dataLoaderModel)).thenReturn(updatedDataLoaderModel);

        DataLoaderModel result = checkInDataLoaderUseCase.checkIn(dataLoaderUuid);

        assertThat(result.getLoadStatus()).isEqualTo(DataLoaderModel.Status.TOO_HIGH);
        assertThat(result.getLastLoadStatusUpdate()).isEqualTo(loadStatusUpdateTime);
    }

    @Test
    @DisplayName("""
            Given data loader does not exist in repository,
            when checking in data loader with non-existent uuid,
            then should throw NotFoundException
            """)
    void should_throwNotFoundExceptionWhenDataLoaderNotFound() {
        String nonExistentUuid = UUID.randomUUID().toString();

        when(dataLoaderRepository.findByUuid(nonExistentUuid))
                .thenThrow(new NotFoundException("No DataLoaderEntity found with uuid " + nonExistentUuid));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> checkInDataLoaderUseCase.checkIn(nonExistentUuid));
        assertThat(exception.getMessage()).contains("No DataLoaderEntity found with uuid");

        verify(dataLoaderRepository).findByUuid(nonExistentUuid);
        verify(timeService, never()).getInstantUTC();
        verify(dataLoaderService, never()).update(any());
    }
}
