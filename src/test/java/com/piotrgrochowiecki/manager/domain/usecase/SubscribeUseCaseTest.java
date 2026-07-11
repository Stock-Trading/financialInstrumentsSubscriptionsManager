package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.model.SubscriptionModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscribeUseCaseTest {

    @Mock
    private DataLoaderRepository dataLoaderRepository;

    @Mock
    private FinancialInstrumentRepository financialInstrumentRepository;

    @InjectMocks
    private SubscribeUseCase subscribeUseCase;

    @Test
    @DisplayName("""
            Given data loader has financial instruments assigned,
            when subscribing,
            then should return subscription with data loader uuid and assigned financial instruments
            """)
    void should_returnSubscriptionWithAssignedFinancialInstruments() {
        String dataLoaderUuid = UUID.randomUUID().toString();
        DataLoaderModel dataLoaderModel = DataLoaderModel.builder()
                .id(3L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(Instant.now())
                .active(true)
                .loadStatus(DataLoaderModel.Status.BALANCED)
                .build();
        FinancialInstrumentModel financialInstrumentModel = FinancialInstrumentModel.builder()
                .id(4L)
                .name("Microsoft Inc.")
                .symbol("MSFT")
                .dataLoaderId(3L)
                .build();
        List<FinancialInstrumentModel> financialInstrumentModelList = List.of(financialInstrumentModel);

        when(dataLoaderRepository.findByUuid(dataLoaderUuid)).thenReturn(dataLoaderModel);
        when(financialInstrumentRepository.findByDataLoaderId(3L)).thenReturn(financialInstrumentModelList);

        SubscriptionModel result = subscribeUseCase.subscribe(dataLoaderUuid);

        assertThat(result).isNotNull();
        assertThat(result.getDataLoaderUUID()).isEqualTo(dataLoaderUuid);
        assertThat(result.getFinancialInstrumentModelCollection()).containsExactly(financialInstrumentModel);
        verify(dataLoaderRepository).findByUuid(dataLoaderUuid);
        verify(financialInstrumentRepository).findByDataLoaderId(3L);
    }

    @Test
    @DisplayName("""
            Given data loader has no financial instruments assigned,
            when subscribing,
            then should return subscription with data loader uuid and empty financial instrument collection
            """)
    void should_returnSubscriptionWithEmptyCollectionWhenNoFinancialInstrumentsAssigned() {
        String dataLoaderUuid = UUID.randomUUID().toString();
        DataLoaderModel dataLoaderModel = DataLoaderModel.builder()
                .id(5L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(Instant.now())
                .active(true)
                .loadStatus(DataLoaderModel.Status.TOO_LOW)
                .build();

        when(dataLoaderRepository.findByUuid(dataLoaderUuid)).thenReturn(dataLoaderModel);
        when(financialInstrumentRepository.findByDataLoaderId(5L)).thenReturn(Collections.emptyList());

        SubscriptionModel result = subscribeUseCase.subscribe(dataLoaderUuid);

        assertThat(result).isNotNull();
        assertThat(result.getDataLoaderUUID()).isEqualTo(dataLoaderUuid);
        assertThat(result.getFinancialInstrumentModelCollection()).isEmpty();
        verify(dataLoaderRepository).findByUuid(dataLoaderUuid);
        verify(financialInstrumentRepository).findByDataLoaderId(5L);
    }

    @Test
    @DisplayName("""
            Given data loader has multiple financial instruments assigned,
            when subscribing,
            then should return subscription containing all assigned financial instruments in order
            """)
    void should_returnSubscriptionWithAllAssignedFinancialInstrumentsInOrder() {
        String dataLoaderUuid = UUID.randomUUID().toString();
        DataLoaderModel dataLoaderModel = DataLoaderModel.builder()
                .id(7L)
                .uuid(dataLoaderUuid)
                .lastConnectedOn(Instant.now())
                .active(true)
                .loadStatus(DataLoaderModel.Status.TOO_HIGH)
                .build();
        FinancialInstrumentModel firstFinancialInstrumentModel = FinancialInstrumentModel.builder()
                .id(4L)
                .name("Microsoft Inc.")
                .symbol("MSFT")
                .dataLoaderId(7L)
                .build();
        FinancialInstrumentModel secondFinancialInstrumentModel = FinancialInstrumentModel.builder()
                .id(8L)
                .name("Apple Inc.")
                .symbol("AAPL")
                .dataLoaderId(7L)
                .build();
        List<FinancialInstrumentModel> financialInstrumentModelList =
                List.of(firstFinancialInstrumentModel, secondFinancialInstrumentModel);

        when(dataLoaderRepository.findByUuid(dataLoaderUuid)).thenReturn(dataLoaderModel);
        when(financialInstrumentRepository.findByDataLoaderId(7L)).thenReturn(financialInstrumentModelList);

        SubscriptionModel result = subscribeUseCase.subscribe(dataLoaderUuid);

        assertThat(result.getFinancialInstrumentModelCollection())
                .containsExactly(firstFinancialInstrumentModel, secondFinancialInstrumentModel);
    }

}
