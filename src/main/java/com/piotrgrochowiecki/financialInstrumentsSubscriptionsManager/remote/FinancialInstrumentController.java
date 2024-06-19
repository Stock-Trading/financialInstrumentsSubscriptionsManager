package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.FinancialInstrumentModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.FinancialInstrumentService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/financialInstrument")
class FinancialInstrumentController {

    private final FinancialInstrumentService service;
    private final ApiMapper mapper;

    @GetMapping("{id}")
    FinancialInstrumentResponseDto handleGetById(@PathVariable("id") @NotNull Long id) {
        FinancialInstrumentModel model = service.getById(id);
        return mapper.mapToResponseDto(model);
    }

    @GetMapping(params = "name")
    FinancialInstrumentResponseDto handleGetByName(@RequestParam("name") @NotBlank String name) {
        FinancialInstrumentModel model = service.getByName(name);
        return mapper.mapToResponseDto(model);
    }

    @GetMapping(params = "symbol")
    FinancialInstrumentResponseDto handleGetBySymbol(@RequestParam("symbol") @NotBlank String symbol) {
        FinancialInstrumentModel model = service.getBySymbol(symbol);
        return mapper.mapToResponseDto(model);
    }

    @GetMapping()
    List<FinancialInstrumentResponseDto> handleGetAll() {
        List<FinancialInstrumentModel> financialInstrumentModelList = service.getAllCurrentlySubscribed();
        return financialInstrumentModelList.stream()
                .map(mapper::mapToResponseDto)
                .toList();
    }

    @PostMapping()
    ResponseEntity<FinancialInstrumentSubscribeResponseDto> handleSubscribe(@RequestBody FinancialInstrumentSubscribeRequestDto subscribeRequestDto) {
        FinancialInstrumentModel modelToBeSubscribed = mapper.mapToModel(subscribeRequestDto);
        FinancialInstrumentModel subscribedModel = service.subscribeToTheInstrument(modelToBeSubscribed);
        return new ResponseEntity<>(mapper.mapToSubscribeResponseDto(subscribedModel), HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    ResponseEntity<FinancialInstrumentUnsubscribeResponseDto> handleUnsubscribeById(@PathVariable @NotNull Long id) {
        FinancialInstrumentModel unsubscribedInstrument = service.unsubscribeFromTheInstrumentById(id);
        return new ResponseEntity<>(mapper.mapToUnsubscribeResponseDto(unsubscribedInstrument), HttpStatus.OK);
    }

    @DeleteMapping(params = "name")
    ResponseEntity<FinancialInstrumentUnsubscribeResponseDto> handleUnsubscribeByName(@RequestParam("name") @NotBlank String name) {
        FinancialInstrumentModel unsubscribedInstrument = service.unsubscribeFromTheInstrumentByName(name);
        return new ResponseEntity<>(mapper.mapToUnsubscribeResponseDto(unsubscribedInstrument), HttpStatus.OK);
    }

    @DeleteMapping(params = "symbol")
    ResponseEntity<FinancialInstrumentUnsubscribeResponseDto> handleUnsubscribeBySymbol(@RequestParam("symbol") @NotBlank String symbol) {
        FinancialInstrumentModel unsubscribedInstrument = service.unsubscribeFromTheInstrumentBySymbol(symbol);
        return new ResponseEntity<>(mapper.mapToUnsubscribeResponseDto(unsubscribedInstrument), HttpStatus.OK);
    }
}

