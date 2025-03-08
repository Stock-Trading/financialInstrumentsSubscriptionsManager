package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

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
class DataLoaderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    String uuid;
    @Column(name = "last_connected_on", nullable = false)
    Instant lastConnectedOn;
    @Column(name = "last_handled_on")
    Instant lastHandledOn;
    @Column(name = "active", nullable = false)
    Boolean active;
    @Column(name = "ready_for_handling", nullable = false)
    Boolean readyForHandling;
    @Column(name = "load_status")
    String loadStatus;
    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "dataLoader")
    Collection<FinancialInstrumentEntity> financialInstrument;
}