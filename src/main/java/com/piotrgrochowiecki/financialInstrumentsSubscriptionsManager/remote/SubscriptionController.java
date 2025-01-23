package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.remote;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.SubscriptionModel;
import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("api/v1/internal/subscription")
class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper mapper;

    @GetMapping("/{dataLoaderUuid}")
    SubscriptionResponseDto handleSubscriptionRequest(@PathVariable String dataLoaderUuid) {
        SubscriptionModel subscriptionModel = subscriptionService.subscribe(dataLoaderUuid);
        return mapper.mapToDto(subscriptionModel);
    }

    //TODO 26.09.2024 rozbić na dwa endpointy: 1) zgłoszenie się dataLoadera (częsty, np. raz na 1s)
    // 2) zapytanie o listę instrumentów do subskrypcji (np. raz na minutę)
    // Potrzebny jest algorytm "tasowania/ rozdzielania" instrumentów finansowych pomiędzy data loadery:
    // wiadoma jest liczba wszystkich data loaderów; można przydzielać
    // dodać pole przy DL "lastHandledOn" pole wskazujące kiedy dataLoader został ostatnio obsłużony


//TODO tu powinien lądować cykliczny request od data loaderów, aplikacja powinna odpowiedzieć wszystkimi
// instrumentami finansowymi, do których dany dataloader powinien subskrybować.
// Potencjalne rozwiązanie: dwie tabelki: 1. informacje o data loaderach (id, uuid_data_loadera, data ostatniego połączenia,
// status zdrowy/ niezdrowy (?)), jeśli uuid jeszcze nie występowało, to dodajemy do tabelki.
// Druga tabelka: pula instrumentów finansowych, które są dostępne w całym systemie (kontrolowana potencjalnie przez admina).
// Pola (id, nazwa instrumentu, symbol)
// Tabela powinna być połączona one-to-many (jeden data loader z wieloma instrumentami finansowymi).
// W tej aplikacji powinien być regularnie uruchamiany serwis sprawdzający jakie mamy data loadery, określić ich status
// na podstawie daty ostatniego połączenia, jeśli status zostanie określony jako niezdrowy (na podstawie daty ostatniego
// połączenia)


// TODO poczytać o lockach na bazie danych: optimistic i pessimistic

}