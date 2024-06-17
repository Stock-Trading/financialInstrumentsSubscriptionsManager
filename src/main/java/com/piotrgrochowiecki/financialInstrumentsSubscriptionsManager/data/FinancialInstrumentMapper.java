package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import org.springframework.stereotype.Component;

@Component
class FinancialInstrumentMapper {

    FinancialInstrumentModel mapToModel(FinancialInstrumentEntity entity) {
        return FinancialInstrumentModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .build();
    }

    FinancialInstrumentEntity mapToEntity(FinancialInstrumentModel model) {
        return FinancialInstrumentEntity.builder()
                .name(model.name())
                .symbol(model.symbol())
                .build();
    }

}
