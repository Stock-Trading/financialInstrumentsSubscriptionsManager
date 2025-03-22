package com.piotrgrochowiecki.manager.remote;

import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
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
