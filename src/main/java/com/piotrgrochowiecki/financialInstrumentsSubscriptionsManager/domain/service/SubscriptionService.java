package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.SubscriptionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final DataLoaderService dataLoaderService;

    public SubscriptionModel subscribe(String dataLoaderUUID) {
        DataLoaderModel dataLoaderModelOptional = dataLoaderService.getByUuid(dataLoaderUUID);
        List<FinancialInstrumentModel> financialInstrumentModelList = dataLoaderModelOptional.getFinancialInstrumentModelCollection().stream().toList();
        return SubscriptionModel.builder()
                .dataLoaderUUID(dataLoaderModelOptional.getUuid())
                .financialInstrumentModelCollection(financialInstrumentModelList)
                .build();
    }

}

