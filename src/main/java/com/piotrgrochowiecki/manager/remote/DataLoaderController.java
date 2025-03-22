package com.piotrgrochowiecki.manager.remote;

import com.piotrgrochowiecki.manager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.manager.domain.usecase.CheckInDataLoaderUseCase;
import com.piotrgrochowiecki.manager.domain.usecase.RegisterDataLoaderUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/internal/dataLoader")
class DataLoaderController {

    private final CheckInDataLoaderUseCase checkInDataLoaderUseCase;
    private final RegisterDataLoaderUseCase registerDataLoaderUseCase;
    private final DataLoaderApiMapper mapper;

    @PostMapping("/{dataLoaderUuid}")
    ResponseEntity<DataLoaderResponseDto> handleRegistrationRequest(@PathVariable String dataLoaderUuid) {
        DataLoaderModel dataLoaderModel = registerDataLoaderUseCase.register(dataLoaderUuid);
        return new ResponseEntity<>(mapper.mapToDto(dataLoaderModel), HttpStatus.CREATED);
    }

    @PutMapping("/{dataLoaderUuid}/last-connection-time")
    DataLoaderResponseDto handleCheckInRequest(@PathVariable String dataLoaderUuid) {
        DataLoaderModel dataLoaderModel = checkInDataLoaderUseCase.checkIn(dataLoaderUuid);
        return mapper.mapToDto(dataLoaderModel);
    }

}
