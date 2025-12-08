package com.piotrgrochowiecki.manager.remote.subscription;

import com.piotrgrochowiecki.manager.domain.model.SubscriptionModel;
import com.piotrgrochowiecki.manager.domain.usecase.SubscribeUseCase;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("api/v1/internal/subscription")
class SubscriptionController {

    private final SubscribeUseCase subscribeUseCase;
    private final SubscriptionMapper mapper;

    @GetMapping("/{dataLoaderUuid}")
    SubscriptionResponseDto handleSubscriptionRequest(@PathVariable String dataLoaderUuid) {
        log.debug("Received request for subscriptions from data loader with uuid {}", dataLoaderUuid);
        SubscriptionModel subscriptionModel = subscribeUseCase.subscribe(dataLoaderUuid);
        return mapper.mapToDto(subscriptionModel);
    }

}