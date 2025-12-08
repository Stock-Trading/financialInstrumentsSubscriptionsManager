package com.piotrgrochowiecki.manager.data.financialinstrument;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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

    @Modifying(flushAutomatically = true,
            clearAutomatically = true)
    @Query("""
            UPDATE FinancialInstrumentEntity fi
            SET fi.dataLoaderId = null
            WHERE fi.id IN :ids
            """)
    int detachDataLoadersBasedOnIds(@Param("ids") List<Long> ids);
}