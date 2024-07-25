package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import org.springframework.data.jpa.repository.JpaRepository;

interface DataLoaderJpaRepository extends JpaRepository<DataLoaderEntity, Long> {

    boolean existsByUuid(String uuid);

}
