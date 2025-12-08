package com.piotrgrochowiecki.manager.data.financialinstrument;

import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

    Sort mapToSort(FinancialInstrumentRepository.OrderBy orderBy) {
        switch (orderBy) {
            case CREATED_ON_ASC -> {
                return Sort.sort(FinancialInstrumentEntity.class)
                        .by(FinancialInstrumentEntity::getCreatedOn)
                        .ascending();
            }
            case CREATED_ON_DESC -> {
                return Sort.sort(FinancialInstrumentEntity.class)
                        .by(FinancialInstrumentEntity::getCreatedOn)
                        .descending();
            }
            default -> throw new IllegalArgumentException("Unknown enum " + orderBy.name());
        }
    }

}
