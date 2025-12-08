package com.piotrgrochowiecki.manager.remote.subscription;

import lombok.Builder;

@Builder
record FinancialInstrumentResponseDto(Long id,
                                      String name,
                                      String symbol) {
}
