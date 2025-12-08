package com.piotrgrochowiecki.manager.remote.dataloader;

import lombok.Builder;

import java.time.Instant;

@Builder
record DataLoaderResponseDto(String uuid,
                             Instant checkedIn) {
}
