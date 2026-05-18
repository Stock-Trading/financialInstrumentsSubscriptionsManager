# Financial Instruments Subscription Manager

> A real-time financial instrument subscription service that intelligently distributes and balances workload across multiple data loaders using advanced load-balancing algorithms.

## Table of Contents

- [About](#about)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Load Balancing Algorithm](#load-balancing-algorithm)
- [Getting Started](#getting-started)
- [Building & Running](#building--running)
- [API Endpoints](#api-endpoints)

## About

This microservice is responsible for the real-time management of Financial Instruments (FIs) assigned to Data Loaders (DLs). Data Loaders register with the service and perform periodic health checks. Financial Instruments are preloaded into the database, and the service manages subscriptions—determining which FIs each Data Loader should handle. The service distributes the workload efficiently across all active Data Loaders using an intelligent round-robin algorithm with automatic rebalancing.

### Key Responsibilities

- **Real-time Subscription Management**: Dynamically assign and reassign Financial Instruments to Data Loaders
- **Health Monitoring**: Track Data Loader connectivity with automatic timeout detection
- **Load Balancing**: Automatically rebalance workload when Data Loaders become unavailable
- **Automatic Recovery**: Self-healing system that gracefully handles failures

## Features

✨ **Core Capabilities**

- 🔄 Automatic load balancing and rebalancing
- ⏱️ Real-time health checks with configurable timeouts
- 🛡️ Automatic failure detection and recovery
- 📊 Fair workload distribution using round-robin algorithm
- 🔌 RESTful API for Data Loader management
- 📈 Scalable batch processing architecture
- 🗄️ Persistent state management with PostgreSQL

## Technology Stack

| Component   | Version     |
|-------------|-------------|
| Java        | 25          |
| Spring Boot | 4.x         |
| PostgreSQL  | Alpine 3.18 |
| Docker      | Latest      |

## Architecture

This service follows **Hexagonal Architecture** (also known as Ports & Adapters), ensuring:

- 🎯 **Domain Independence**: Core business logic isolated from external dependencies
- 🔌 **Plugin Architecture**: Adapters can be easily swapped or extended
- 🧪 **Testability**: Domain logic can be tested without external services
- 📚 **Domain-Driven Design**: Clear separation of concerns

### Core Domain Models

- **Data Loader**: Represents a client process that processes financial instruments
- **Financial Instrument**: A tradeable asset with metadata
- **Subscription**: The assignment of Financial Instruments to a specific Data Loader

## Project Structure

```
financialInstrumentsSubscriptionsManager/
├── src/main/java/com/piotrgrochowiecki/manager/
│   ├── FinancialInstrumentsSubscriptionsManagerApplication.java
│   ├── data/                      # Data layer (repositories, entities, mappers)
│   │   ├── dataloader/
│   │   └── financialinstrument/
│   ├── domain/                    # Business logic (models, services, use cases)
│   │   ├── component/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── port/                  # Domain interfaces
│   │   ├── service/
│   │   └── usecase/
│   └── remote/                    # REST controllers (adapters)
│       ├── dataloader/
│       └── subscription/
├── src/main/resources/
│   └── application.properties     # Configuration
├── docker-compose.yml             # Database setup
├── pom.xml                        # Maven configuration
└── README.md
```

## Load Balancing Algorithm

This service implements a sophisticated load-balancing algorithm that:

- 📊 **Continuously Monitors** Data Loader health and workload status
- ⚡ **Rapidly Detects** failures within 1 second
- 🔄 **Automatically Rebalances** workload across healthy Data Loaders
- ✅ **Recovers** completely within 10 seconds of failure detection
- 🎯 **Ensures Fair Distribution** using ordered round-robin assignment

For detailed algorithm documentation, including state machines, workflows, and failure scenarios, see:

📄 **`src/main/java/com/piotrgrochowiecki/manager/domain/service/LOAD_BALANCING_ALGORITHM.md`**

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd financialInstrumentsSubscriptionsManager
```

### 2. Set Up the Database

Start PostgreSQL using Docker Compose:

```bash
docker-compose up -d
```

This will start a PostgreSQL instance with the required database and configuration.

## Building & Running

### Build the Application

Using Maven wrapper (Windows):

```bash
mvnw.cmd clean install
```

Using Maven wrapper (macOS/Linux):

```bash
./mvnw clean install
```

### Run the Application

```bash
mvnw.cmd spring-boot:run
```

The service will start on `http://localhost:8080`

### Run with Docker

*(Optional) Build a Docker image and run:*

```bash
docker build -t financial-instruments-manager .
docker run -p 8080:8080 --network financialInstrumentsSubscriptionsManager_default financial-instruments-manager
```

## API Endpoints

### Data Loader Management

#### Register a Data Loader

**Request:**
```http
POST /api/v1/internal/dataLoader/{dataLoaderUuid}
```

**Response:** `201 Created`
```json
{
  "uuid": "uuid1",
  "checkedIn": "2026-05-17T10:30:00Z"
}
```

#### Health Check / Check-in

**Request:**
```http
PUT /api/v1/internal/dataLoader/{dataLoaderUuid}/last-connection-time
```

**Response:** `200 OK`
```json
{
  "uuid": "uuid1",
  "checkedIn": "2026-05-17T10:35:00Z"
}
```

### Subscription Management

#### Get Subscriptions for Data Loader

**Request:**
```http
GET /api/v1/internal/subscription/{dataLoaderUuid}
```

**Response:** `200 OK`
```json
{
  "dataLoaderUuid": "uuid1",
  "financialInstrumentResponseDtoList": [
    {
      "id": 1,
      "symbol": "AAPL"
    },
    {
      "id": 2,
      "symbol": "GOOGL"
    }
  ]
}
```

### Testing Endpoints

A Postman collection is included at `src/test/resources/Stock Trading - Subscriptions Manager.postman_collection.json` for easy API testing.

## License

This project is proprietary software developed by Piotr Grochowiecki. All rights reserved.

---

**For questions or issues**, please email Piotr.Grochowiecki@gmail.com.
