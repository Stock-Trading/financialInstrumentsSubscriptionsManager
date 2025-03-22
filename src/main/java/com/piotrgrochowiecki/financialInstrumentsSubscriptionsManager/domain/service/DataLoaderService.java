package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.component.DataLoaderParametersProvider;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception.DataLoaderServiceException;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.exception.ModelAlreadyExistsException;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.DataLoaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final DataLoaderRepository dataLoaderRepository;

    @Transactional
    public DataLoaderModel update(DataLoaderModel dataLoaderModel) {
        if (Objects.isNull(dataLoaderModel.getId())) {
            throw new DataLoaderServiceException("Cannot update Data Loader as its id is null");
        }
        return dataLoaderRepository.save(dataLoaderModel);
    }

}
