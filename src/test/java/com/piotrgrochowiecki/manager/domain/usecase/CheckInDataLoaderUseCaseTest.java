package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.exception.NotFoundException;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.DataLoaderService;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInDataLoaderUseCaseTest {

    @Mock
    private DataLoaderService dataLoaderService;

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private TimeService timeService;

    @InjectMocks
    private CheckInDataLoaderUseCase checkInDataLoaderUseCase;

    private DataLoaderModel dataLoaderModel;

    @BeforeEach
    void setup() {
        dataLoaderModel = DataLoaderModel.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .active(false)
                .readyForAssignmentOfFinancialInstruments(false)
                .build();
    }

    @Test
    void testCheckInSuccessful() {
        // Arrange
        when(dataLoaderRepository.findByUuid(dataLoaderModel.getUuid())).thenReturn(dataLoaderModel);
        Instant now = Instant.now();
        when(timeService.getInstantUTC()).thenReturn(now);
        when(dataLoaderService.update(any(DataLoaderModel.class))).thenReturn(dataLoaderModel);

        // Act
        DataLoaderModel updatedModel = checkInDataLoaderUseCase.checkIn(dataLoaderModel.getUuid());

        // Assert
        assertEquals(now, updatedModel.getLastConnectedOn());
        assertTrue(updatedModel.getActive());
        assertTrue(updatedModel.getReadyForHandling());
        verify(dataLoaderService, times(1)).update(any(DataLoaderModel.class));
    }

    @Test
    void testCheckInDataLoaderNotFound() {
        // Arrange
        String unknownUuid = UUID.randomUUID().toString();
        when(dataLoaderRepository.findByUuid(unknownUuid)).thenThrow(NotFoundException.class);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> checkInDataLoaderUseCase.checkIn(unknownUuid));
        verify(dataLoaderService, never()).update(any(DataLoaderModel.class));
    }

}
