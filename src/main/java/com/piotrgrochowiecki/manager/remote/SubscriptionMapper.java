package com.piotrgrochowiecki.manager.remote;

import com.piotrgrochowiecki.manager.domain.model.SubscriptionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SubscriptionMapper {

    private final FinancialInstrumentApiMapper apiMapper;

    SubscriptionResponseDto mapToDto(SubscriptionModel model) {
        return SubscriptionResponseDto.builder()
                .dataLoaderUuid(model.getDataLoaderUUID())
                .financialInstrumentResponseDtoList(model.getFinancialInstrumentModelCollection().stream()
                        .map(apiMapper::mapToDto)
                        .toList())
                .build();
    }
}
