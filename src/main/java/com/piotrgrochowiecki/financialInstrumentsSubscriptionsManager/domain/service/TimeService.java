package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class TimeService {

    public Instant getInstantUTC() {
        return Clock.systemUTC().instant();
    }
}
