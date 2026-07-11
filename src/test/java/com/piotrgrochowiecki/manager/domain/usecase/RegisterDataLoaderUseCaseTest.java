package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.exception.ModelAlreadyExistsException;
import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.service.TimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterDataLoaderUseCaseTest {

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private TimeService timeService;

    @InjectMocks
    private RegisterDataLoaderUseCase registerDataLoaderUseCase;

    @Test
    @DisplayName("""
            Given data loader uuid does not exist in repository,
            when registering data loader,
            then should save and return new active data loader with TOO_LOW load status
            """)
    void should_saveAndReturnNewDataLoaderWhenUuidNotRegistered() {
        String dataLoaderUuid = UUID.randomUUID().toString();
        Instant currentTime = Instant.now();
        DataLoaderModel savedDataLoaderModel = DataLoaderModel.builder()
                .id(3L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(currentTime)
                .active(true)
                .loadStatus(DataLoaderModel.Status.TOO_LOW)
                .build();
        ArgumentCaptor<DataLoaderModel> dataLoaderModelCaptor = ArgumentCaptor.forClass(DataLoaderModel.class);

        when(dataLoaderRepository.existsByUuid(dataLoaderUuid)).thenReturn(false);
        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderRepository.save(any(DataLoaderModel.class))).thenReturn(savedDataLoaderModel);

        DataLoaderModel result = registerDataLoaderUseCase.register(dataLoaderUuid);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(savedDataLoaderModel);
        verify(dataLoaderRepository).existsByUuid(dataLoaderUuid);
        verify(timeService).getInstantUTC();
        verify(dataLoaderRepository).save(dataLoaderModelCaptor.capture());
        DataLoaderModel modelPassedToSave = dataLoaderModelCaptor.getValue();
        assertThat(modelPassedToSave.getUuid()).isEqualTo(dataLoaderUuid);
        assertThat(modelPassedToSave.getLastConnectedOn()).isEqualTo(currentTime);
        assertThat(modelPassedToSave.getActive()).isTrue();
        assertThat(modelPassedToSave.getLoadStatus()).isEqualTo(DataLoaderModel.Status.TOO_LOW);
    }

    @Test
    @DisplayName("""
            Given data loader uuid already exists in repository,
            when registering data loader,
            then should throw ModelAlreadyExistsException and never save
            """)
    void should_throwModelAlreadyExistsExceptionWhenUuidAlreadyRegistered() {
        String dataLoaderUuid = UUID.randomUUID().toString();

        when(dataLoaderRepository.existsByUuid(dataLoaderUuid)).thenReturn(true);

        ModelAlreadyExistsException exception = assertThrows(ModelAlreadyExistsException.class,
                () -> registerDataLoaderUseCase.register(dataLoaderUuid));

        assertThat(exception.getMessage()).contains(dataLoaderUuid);
        verify(dataLoaderRepository).existsByUuid(dataLoaderUuid);
        verify(timeService, never()).getInstantUTC();
        verify(dataLoaderRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            Given data loader uuid does not exist in repository,
            when registering data loader,
            then should pass data loader model with correct uuid, lastConnectedOn, active and loadStatus to repository
            """)
    void should_passCorrectDataLoaderModelFieldsToRepositoryWhenRegistering() {
        String dataLoaderUuid = UUID.randomUUID().toString();
        Instant currentTime = Instant.now();
        ArgumentCaptor<DataLoaderModel> dataLoaderModelCaptor = ArgumentCaptor.forClass(DataLoaderModel.class);

        when(dataLoaderRepository.existsByUuid(dataLoaderUuid)).thenReturn(false);
        when(timeService.getInstantUTC()).thenReturn(currentTime);
        when(dataLoaderRepository.save(any(DataLoaderModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registerDataLoaderUseCase.register(dataLoaderUuid);

        verify(dataLoaderRepository).save(dataLoaderModelCaptor.capture());
        DataLoaderModel savedDataLoaderModel = dataLoaderModelCaptor.getValue();
        assertThat(savedDataLoaderModel.getUuid()).isEqualTo(dataLoaderUuid);
        assertThat(savedDataLoaderModel.getLastConnectedOn()).isEqualTo(currentTime);
        assertThat(savedDataLoaderModel.getActive()).isTrue();
        assertThat(savedDataLoaderModel.getLoadStatus()).isEqualTo(DataLoaderModel.Status.TOO_LOW);
    }

}
