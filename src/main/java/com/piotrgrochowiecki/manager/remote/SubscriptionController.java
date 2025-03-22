package com.piotrgrochowiecki.manager.remote;

import com.piotrgrochowiecki.manager.domain.model.SubscriptionModel;
import com.piotrgrochowiecki.manager.domain.usecase.SubscribeUseCase;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("api/v1/internal/subscription")
class SubscriptionController {

    private final SubscribeUseCase subscribeUseCase;
    private final SubscriptionMapper mapper;

    @GetMapping("/{dataLoaderUuid}")
    SubscriptionResponseDto handleSubscriptionRequest(@PathVariable String dataLoaderUuid) {
        SubscriptionModel subscriptionModel = subscribeUseCase.subscribe(dataLoaderUuid);
        return mapper.mapToDto(subscriptionModel);
    }

}