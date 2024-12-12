package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.SubscriptionModel;
import org.springframework.stereotype.Component;

@Component
class ApiMapper {

    FinancialInstrumentResponseDto mapToDto(FinancialInstrumentModel model) {
        return FinancialInstrumentResponseDto.builder()
                .id(model.getId())
                .name(model.getName())
                .symbol(model.getSymbol())
                .build();
    }

    SubscriptionResponseDto mapToDto(SubscriptionModel model) {
        return SubscriptionResponseDto.builder()
                .dataLoaderUuid(model.getDataLoaderUUID())
                .financialInstrumentResponseDtoList(model.getFinancialInstrumentModelCollection().stream()
                        .map(this::mapToDto)
                        .toList())
                .build();
    }

    DataLoaderResponseDto mapToDto(DataLoaderModel model) {
        return DataLoaderResponseDto.builder()
                .uuid(model.getUuid())
                .checkedIn(model.getLastConnectedOn())
                .build();
    }

}
