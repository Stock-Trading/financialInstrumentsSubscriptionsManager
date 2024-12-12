package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.DataLoaderModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.DataLoaderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/internal/dataLoader")
class DataLoaderController {

    private final DataLoaderService dataLoaderService;
    private final ApiMapper mapper;

    @PostMapping("/{dataLoaderUuid}")
    ResponseEntity<DataLoaderResponseDto> handleRegistrationRequest(@PathVariable String dataLoaderUuid) {
        DataLoaderModel dataLoaderModel = dataLoaderService.register(dataLoaderUuid);
        return new ResponseEntity<>(mapper.mapToDto(dataLoaderModel), HttpStatus.CREATED);
    }

    @PutMapping("/{dataLoaderUuid}/last-connection-time")
    DataLoaderResponseDto handleCheckInRequest(@PathVariable String dataLoaderUuid) {
        DataLoaderModel dataLoaderModel = dataLoaderService.checkIn(dataLoaderUuid);
        return mapper.mapToDto(dataLoaderModel);
    }

}
