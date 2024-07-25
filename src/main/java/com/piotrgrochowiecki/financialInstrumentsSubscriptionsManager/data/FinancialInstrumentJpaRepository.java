package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface FinancialInstrumentJpaRepository extends JpaRepository<FinancialInstrumentEntity, Long> {

    FinancialInstrumentEntity getByName(String name);

    FinancialInstrumentEntity getBySymbol(String symbol);

    @Query("SELECT f.symbol FROM FinancialInstrumentEntity f")
    List<String> getAllSymbols();

    void deleteById(Long id);

    void deleteByName(String name);

    void deleteBySymbol(String symbol);

    boolean existsByName(String name);

    boolean existsBySymbol(String symbol);

}
