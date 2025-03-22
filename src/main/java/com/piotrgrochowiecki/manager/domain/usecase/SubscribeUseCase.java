package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.model.SubscriptionModel;
import com.piotrgrochowiecki.manager.domain.ports.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class SubscribeUseCase {

    private final DataLoaderRepository dataLoaderRepository;

    public SubscriptionModel subscribe(String dataLoaderUUID) {
        DataLoaderModel dataLoaderModelOptional = dataLoaderRepository.findByUuid(dataLoaderUUID);
        List<FinancialInstrumentModel> financialInstrumentModelList = dataLoaderModelOptional.getFinancialInstrumentModelCollection()
                .stream()
                .toList();
        return SubscriptionModel.builder()
                .dataLoaderUUID(dataLoaderModelOptional.getUuid())
                .financialInstrumentModelCollection(financialInstrumentModelList)
                .build();
    }

}

