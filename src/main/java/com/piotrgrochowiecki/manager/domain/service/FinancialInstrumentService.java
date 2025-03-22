package com.piotrgrochowiecki.manager.domain.service;

import com.piotrgrochowiecki.manager.domain.exception.FinancialInstrumentServiceException;
import com.piotrgrochowiecki.manager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.manager.domain.ports.FinancialInstrumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Log4j2
@Service
@AllArgsConstructor
public class FinancialInstrumentService {

    private final FinancialInstrumentRepository financialInstrumentRepository;

    @Transactional
    public void update(FinancialInstrumentModel financialInstrumentModel) {
        if (Objects.isNull(financialInstrumentModel.getId())) {
            throw new FinancialInstrumentServiceException("Cannot update Financial Instrument as its id is null");
        }
        financialInstrumentRepository.save(financialInstrumentModel);
    }

}

