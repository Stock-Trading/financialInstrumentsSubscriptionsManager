package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote.external;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Component;

@Component
class ApiMapper {

    FinancialInstrumentModel mapToModel(FinancialInstrumentSubscribeRequestDto requestDto) {
        return FinancialInstrumentModel.builder()
                .name(requestDto.name())
                .symbol(requestDto.symbol())
                .build();
    }

    FinancialInstrumentSubscribeResponseDto mapToSubscribeResponseDto(FinancialInstrumentModel model) {
        return FinancialInstrumentSubscribeResponseDto.builder()
                .id(model.id())
                .name(model.name())
                .symbol(model.symbol())
                .build();
    }

    FinancialInstrumentUnsubscribeResponseDto mapToUnsubscribeResponseDto(FinancialInstrumentModel model) {
        return FinancialInstrumentUnsubscribeResponseDto.builder()
                .id(model.id())
                .name(model.name())
                .symbol(model.symbol())
                .build();
    }

    FinancialInstrumentResponseDto mapToResponseDto(FinancialInstrumentModel model) {
        return FinancialInstrumentResponseDto.builder()
                .id(model.id())
                .name(model.name())
                .symbol(model.symbol())
                .build();
    }

}
