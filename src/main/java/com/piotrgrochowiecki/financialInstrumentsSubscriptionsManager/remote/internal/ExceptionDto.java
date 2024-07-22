package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote.internal;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
record ExceptionDto(String message,
                    LocalDateTime timeStamp) {
}
