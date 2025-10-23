package com.piotrgrochowiecki.manager.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.Arrays;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
public class DataLoaderModel {

    private Long id;
    private String uuid;
    private Instant lastConnectedOn;
    private Instant lastInstantOfFinancialInstrumentsAssignment;
    /**
     * This status is set based on last connection time (lastConnectedOn) of the Data Loader
     */
    private Boolean active;
    private Status loadStatus;
    //TODO rozważyć dodanie liczby aktualnie przypisanych instrumentów, która jest na bieżąco aktualizowana

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        TOO_HIGH("TOO_HIGH"),
        TOO_LOW("TOO_LOW"),
        BALANCED("BALANCED");

        private final String dbValue;

        public static Status getStatusByDbValue(String dbValue) {
            return Arrays.stream(Status.values())
                    .filter(it -> it.dbValue
                            .equals(dbValue))
                    .findFirst()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Unknown dbValue")
                    );
        }
    }

}
