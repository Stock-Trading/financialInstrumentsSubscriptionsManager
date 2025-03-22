package com.piotrgrochowiecki.manager.data;

import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class FinancialInstrumentEntityMapper {

    FinancialInstrumentModel mapToFinancialInstrumentModel(FinancialInstrumentEntity entity) {
        return FinancialInstrumentModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .dataLoaderId(entity.getDataLoaderId())
                .build();
    }

    FinancialInstrumentEntity mapToFinancialInstrumentEntity(FinancialInstrumentModel model) {
        return FinancialInstrumentEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .symbol(model.getSymbol())
                .dataLoaderId(model.getDataLoaderId())
                .build();
    }

}
