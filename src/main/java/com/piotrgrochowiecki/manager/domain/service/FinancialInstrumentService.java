package com.piotrgrochowiecki.manager.domain.service;

import com.piotrgrochowiecki.manager.domain.exception.FinancialInstrumentServiceException;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.port.FinancialInstrumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class FinancialInstrumentService {

    private final FinancialInstrumentRepository financialInstrumentRepository;

    @Transactional
    public FinancialInstrumentModel update(FinancialInstrumentModel financialInstrumentModel) {
        return Optional.of(financialInstrumentModel)
                .filter(it -> Objects.nonNull(it.getId()))
                .map(financialInstrumentRepository::save)
                .orElseThrow(() -> new FinancialInstrumentServiceException("Cannot update Financial Instrument as its id is null"));
    }

}

