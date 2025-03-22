package com.piotrgrochowiecki.manager.remote;

import lombok.Builder;

import java.time.Instant;

@Builder
record DataLoaderResponseDto(String uuid,
                             Instant checkedIn) {
}
