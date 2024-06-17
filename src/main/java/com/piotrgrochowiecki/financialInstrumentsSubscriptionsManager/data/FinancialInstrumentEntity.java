package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.data;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "financial_instruments")
@Data
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinancialInstrumentEntity {
    
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

}
