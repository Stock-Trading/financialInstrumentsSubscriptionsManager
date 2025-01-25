package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Component;

@Component
class FinancialInstrumentApiMapper {

    FinancialInstrumentResponseDto mapToDto(FinancialInstrumentModel model) {
        return FinancialInstrumentResponseDto.builder()
                .id(model.getId())
                .name(model.getName())
                .symbol(model.getSymbol())
                .build();
    }

}
