package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data.dataLoader;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data.financialInstrument.FinancialInstrumentEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Collection;

@Entity
@Table(name = "data_loader")
@Data
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataLoaderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column
    String uuid;
    @Column(name = "last_connected_on", nullable = false)
    Instant lastConnected;
    @OneToMany(cascade = CascadeType.ALL,
    fetch = FetchType.LAZY,
    mappedBy = "dataLoader")
    Collection<FinancialInstrumentEntity> financialInstrument;

}
