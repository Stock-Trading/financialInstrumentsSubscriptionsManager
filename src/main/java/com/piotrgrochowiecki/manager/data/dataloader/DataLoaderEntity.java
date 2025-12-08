package com.piotrgrochowiecki.manager.data.dataloader;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;

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
    @Column(name = "last_instant_of_financial_instruments_assignment")
    Instant lastInstantOfFinancialInstrumentsAssignment;
    @Column(name = "active", nullable = false)
    Boolean active;
    @Column(name = "load_status")
    String loadStatus;
    @Column(name = "last_load_status_update")
    Instant lastLoadStatusUpdate;
}