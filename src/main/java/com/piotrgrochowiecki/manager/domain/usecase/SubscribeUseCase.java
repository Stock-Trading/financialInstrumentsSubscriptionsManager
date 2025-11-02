package com.piotrgrochowiecki.manager.domain.usecase;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.model.SubscriptionModel;
import com.piotrgrochowiecki.manager.domain.port.DataLoaderRepository;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class SubscribeUseCase {

    private final DataLoaderRepository dataLoaderRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;

    @Transactional
    public SubscriptionModel subscribe(String dataLoaderUUID) {
        DataLoaderModel dataLoaderModel = dataLoaderRepository.findByUuid(dataLoaderUUID);
        List<FinancialInstrumentModel> financialInstrumentModelList = financialInstrumentRepository.findByDataLoaderId(
                        dataLoaderModel.getId())
                .stream()
                .toList();
        log.info("Obtaining subscription for Data loader with uuid {}. " +
                        "List of Financial Instruments to subscribe: {}",
                dataLoaderUUID,
                financialInstrumentModelList);
        return SubscriptionModel.builder()
                .dataLoaderUUID(dataLoaderModel.getUuid())
                .financialInstrumentModelCollection(financialInstrumentModelList)
                .build();
    }

}

