package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.ports.FinancialInstrumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class FinancialInstrumentRepositoryImpl implements FinancialInstrumentRepository {

    private final FinancialInstrumentJpaRepository jpaRepository;
    private final DataMapper mapper;

    @Override
    public Optional<FinancialInstrumentModel> findById(Long id) {
        try {
            FinancialInstrumentEntity entity = jpaRepository.getReferenceById(id);
            return Optional.of(mapper.mapToFinancialInstrumentModel(entity));
        } catch (EntityNotFoundException entityNotFoundException) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FinancialInstrumentModel> findByName(String name) {
        try {
            FinancialInstrumentEntity entity = jpaRepository.getByName(name);
            return Optional.of(mapper.mapToFinancialInstrumentModel(entity));
        } catch (EntityNotFoundException entityNotFoundException) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FinancialInstrumentModel> findBySymbol(String symbol) {
        try {
            FinancialInstrumentEntity entity = jpaRepository.getBySymbol(symbol);
            return Optional.of(mapper.mapToFinancialInstrumentModel(entity));
        } catch (EntityNotFoundException entityNotFoundException) {
            return Optional.empty();
        }
    }

    @Override
    public List<FinancialInstrumentModel> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::mapToFinancialInstrumentModel)
                .toList();
    }

    @Override
    public List<String> findAllSymbols() {
        return jpaRepository.getAllSymbols();
    }

    @Override
    public FinancialInstrumentModel save(FinancialInstrumentModel model) {
        FinancialInstrumentEntity entity = mapper.mapToFinancialInstrumentEntity(model);
        FinancialInstrumentEntity savedEntity = jpaRepository.save(entity);
        return mapper.mapToFinancialInstrumentModel(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByName(String name) {
        jpaRepository.deleteByName(name);
    }

    @Override
    public void deleteBySymbol(String symbol) {
        jpaRepository.deleteBySymbol(symbol);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsBySymbol(String symbol) {
        return jpaRepository.existsBySymbol(symbol);
    }
}
