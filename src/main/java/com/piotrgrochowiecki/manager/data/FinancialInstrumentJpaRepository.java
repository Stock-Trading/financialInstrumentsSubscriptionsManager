package com.piotrgrochowiecki.manager.data;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

interface FinancialInstrumentJpaRepository extends JpaRepository<FinancialInstrumentEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
        //Sets up SKIP LOCKED
    List<FinancialInstrumentEntity> findByDataLoaderIdIsNull(Pageable pageable);

    boolean existsByDataLoaderIdIsNull();

    List<FinancialInstrumentEntity> findByDataLoaderId(Long dataLoaderId);

    Long countByDataLoaderId(Long dataLoaderId);
}