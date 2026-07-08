# Project overview
This service a manager of subscriptions (which is a list of Financial Instruments) for which Data Loaders 
(another service in the system) sends HTTP requests. 
Data Loaders on their startup register with this service. This, through round-robin load balancing algorithm, allows 
to assign Financial Instruments to active Data Loaders. 
Data Loaders are expected to send check in requests in regular time intervals, otherwise they are considered unhealthy
and are omitted during assignment of Financial Instruments.
Financial Instruments are preloaded into database.
For details on how load-balancing algorithm is implemented, see file located in
`src/main/java/com/piotrgrochowiecki/manager/domain/service/LOAD_BALANCING_ALGORITHM.md`
Individual use case (which are parts of algorithm) files all have diagrams for visual representation of state changes
and data flow

# Tech stack
- Java 25
- SpringBoot 4.x
- PostgresQL
- Docker
- JUnit 6
- Maven

# Architecture
This application is build using hexagonal (also known as port and adapters) architectural style. At the core, there is
a domain package which contains business logic. It follows Domain Driven Design rules.

# Project Structure

```
financialInstrumentsSubscriptionsManager/
├── src/main/java/com/piotrgrochowiecki/manager/
│   ├── FinancialInstrumentsSubscriptionsManagerApplication.java
│   ├── data/                      # Data adapter (repositories, entities, mappers)
│   │   ├── dataloader/
│   │   └── financialinstrument/
│   ├── domain/                    # Business logic (models, services, use cases)
│   │   ├── component/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── port/                  # Ports for communication with adapters
│   │   ├── service/
│   │   └── usecase/
│   └── remote/                    # API - web adapter
│       ├── dataloader/
│       └── subscription/
├── src/main/resources/
│   └── application.properties     # Configuration
├── docker-compose.yml             # Database setup
├── pom.xml                        # Maven configuration
└── README.md
```

# Base domain models
- Financial Instrument
It is a unique tradable entity, usually stock, but could be also crypto, commodity, etc.
- Data Loader
It is another service of microservices based system. It is used to load data about any given financial instrument
from publicly available APIs
- Subscription
A list of Financial Instruments for which Data Loader should obtain real-time price information 

# Configurable parameters explained
- dataLoader.activeThreshold.millisecond (default: 10000ms) - This is a parameter that describes a health threshold of 
Data Loader. If there is no check-in request every 10_000ms from any given Data Loader, algorithm marks it as inactive
- dataLoader.recommendedNumberOfFinancialInstrumentsPerDataLoader (default: 5) - Because all services are stateless and 
hosted on kubernetes with multiple replicas, this parameter ensures fairness between all instances 
- dataLoader.numberOfDataLoadersHandledByManagerInOneReassignmentCycle (default: 2) - Exists for the same reason as above
- spring.task.scheduling.pool.size (default: 10) - Arbitrary number of threads in Spring's scheduling threadpool

# Core use cases
- Register Data Loader
- Check-in (health check)
- Subscribe/Get Subscriptions
- Load balancing workflows (5 scheduled tasks)

# Code guidelines
Follow standard Java conventions and best practices. For formatting, follow IntelliJ default formatting.