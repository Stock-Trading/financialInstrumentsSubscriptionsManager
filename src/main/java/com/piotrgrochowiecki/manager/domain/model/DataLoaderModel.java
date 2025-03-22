package com.piotrgrochowiecki.manager.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class DataLoaderModel {

    private Long id;
    private String uuid;
    private Instant lastConnectedOn;
    private Instant lastHandledOn;
    private Boolean active;
    private Boolean readyForHandling;
    private Status loadStatus;
    private Collection<FinancialInstrumentModel> financialInstrumentModelCollection;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        TOO_HIGH("too_high"),
        TOO_LOW("too_low"),
        BALANCED("balanced");

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
