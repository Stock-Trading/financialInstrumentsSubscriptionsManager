package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "financial_instrument")
@Data
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
class FinancialInstrumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    String name;
    @Column(unique = true)
    String symbol;
    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    Instant createdOn;
    @Column(name = "data_loader_id")
    Long dataLoaderId;
    @ManyToOne
    @JoinColumn(name = "data_loader_id", referencedColumnName = "id", insertable = false, updatable = false)
    DataLoaderEntity dataLoader;
}
