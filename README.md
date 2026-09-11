# TradeMatch Exchange

A backend electronic trading platform built with Java and Spring Boot that models core behaviour of an exchange order-matching system.

TradeMatch maintains in-memory order books using price-time priority, supports multiple execution instructions, executes full and partial fills, persists active orders and completed trades to PostgreSQL, restores active exchange state after restart, and exposes order-management functionality through a REST API.

The project focuses on correctness of matching behaviour, state consistency, persistence, API design, automated testing, and CI rather than UI development.

---

## Key Features

### Matching Engine

- Price-time priority matching
- Separate BUY and SELL priority queues
- Highest-priced BUY order receives price priority
- Lowest-priced SELL order receives price priority
- Earlier orders receive priority when prices are equal
- Full and partial order fills
- Multi-level order-book sweeping
- Automatic trade generation
- UUID-based trade identifiers

### Supported Order Instructions

| Instruction | Behaviour |
|---|---|
| `LIMIT` | Executes only at the specified price or better; unmatched quantity may remain on the book |
| `MARKET` | Executes immediately against available liquidity across price levels |
| `IOC` | Executes immediately for available quantity and cancels the remaining quantity |
| `FOK` | Executes only when the entire requested quantity can be filled immediately at compatible prices |

### Order Management

- Place orders
- Retrieve active orders
- Retrieve individual orders
- Modify quantity and price
- Cancel orders
- Maintain order priority correctly after modification
- Reject invalid order requests
- Reject duplicate active order IDs with HTTP `409 Conflict`

### Persistence and Recovery

- PostgreSQL persistence using Spring Data JPA
- Active order persistence
- Executed trade persistence
- Startup recovery of active orders
- Original timestamps preserved during recovery
- Runtime and persisted order state kept consistent after updates and executions

### REST API

- Spring Boot REST API
- Request validation
- Structured error responses
- Appropriate HTTP status codes
- Swagger / OpenAPI documentation

### Testing and CI

- Unit tests
- Service tests
- Controller tests
- Repository tests
- REST integration tests
- H2-backed isolated integration testing
- Maven automated build
- GitHub Actions continuous integration

The current automated test suite contains **70 tests** covering the matching engine, order-book behaviour, persistence, service logic, API behaviour, validation, error handling, and integration flows.

---

## Architecture

```text
                         REST CLIENT
                             |
                             v
                    +------------------+
                    | OrderController  |
                    +------------------+
                             |
                             v
                    +------------------+
                    |   OrderService   |
                    +------------------+
                       |            |
                       |            +--------------------+
                       v                                 v
                +-------------+                  +----------------+
                |  Exchange   |                  | OrderRepository|
                +-------------+                  +----------------+
                       |                                 |
                       v                                 |
                +-------------+                         |
                |  OrderBook  |                         |
                +-------------+                         |
                  |         |                           |
                  v         v                           |
            BUY Priority  SELL Priority                 |
               Queue         Queue                      |
                  \           /                         |
                   \         /                          |
                    v       v                           |
                 +----------------+                     |
                 | MatchingEngine |                     |
                 +----------------+                     |
                         |                              |
                         v                              |
                    +---------+                         |
                    |  Trade  |                         |
                    +---------+                         |
                         |                              |
                         v                              v
                  +------------------------------------------+
                  |                PostgreSQL                |
                  |          Orders + Executed Trades        |
                  +------------------------------------------+
```

### Request Flow

```text
HTTP Request
    |
    v
OrderController
    |
    v
OrderService
    |
    +----> PostgreSQL persistence
    |
    v
Exchange
    |
    v
OrderBook
    |
    v
MatchingEngine
    |
    v
Trade Execution
```

The REST layer is separated from the exchange domain logic. The matching engine operates on in-memory order books for fast access, while PostgreSQL provides durable storage for active orders and trade history.

---

## Price-Time Priority

TradeMatch uses price-time priority, a common ordering rule in electronic markets.

For BUY orders:

```text
Higher price
    ↓
Earlier timestamp
```

For SELL orders:

```text
Lower price
    ↓
Earlier timestamp
```

Example BUY book:

```text
BUY-A    €205    10:00
BUY-B    €205    10:01
BUY-C    €200    09:59
```

Execution priority:

```text
BUY-A
BUY-B
BUY-C
```

Although `BUY-C` arrived earlier, the higher-priced orders receive priority first.

When an order's price or quantity is modified, TradeMatch assigns it a new timestamp and reinserts it into the priority queue. This causes the modified order to lose its previous time priority while keeping the in-memory and persisted state consistent.

---

## Matching Example

Suppose the SELL book contains:

```text
SELL-1    30 shares    €190
SELL-2    40 shares    €195
SELL-3    50 shares    €205
```

A MARKET BUY for 70 shares sweeps available liquidity:

```text
30 shares matched with SELL-1 @ €190
40 shares matched with SELL-2 @ €195
```

The incoming order is completely filled across two price levels.

The matching engine also handles partial fills by reducing the remaining quantity of an order rather than incorrectly removing the entire order.

---

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| REST | Spring Web |
| Persistence | Spring Data JPA / Hibernate |
| Production Database | PostgreSQL |
| Test Database | H2 |
| Validation | Jakarta Bean Validation |
| API Documentation | Springdoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc |
| Build | Maven |
| CI | GitHub Actions |
| Version Control | Git / GitHub |

---

## REST API

Base endpoint:

```text
/orders
```

### Create an Order

```http
POST /orders
Content-Type: application/json
```

Example:

```json
{
  "orderId": "BUY-1001",
  "stockSymbol": "AAPL",
  "quantity": 100,
  "price": 200.0,
  "orderType": "BUY",
  "executionType": "LIMIT"
}
```

### Get Active Orders

```http
GET /orders
```

### Get an Order

```http
GET /orders/{orderId}
```

### Modify an Order

```http
PUT /orders/{orderId}
Content-Type: application/json
```

Example:

```json
{
  "quantity": 150,
  "price": 205.0
}
```

### Cancel an Order

```http
DELETE /orders/{orderId}
```

---

## Error Handling

The API returns structured errors instead of exposing internal exceptions.

Example:

```json
{
  "timestamp": "2026-09-10T00:00:00",
  "status": 404,
  "error": "Order Not Found",
  "message": "Order not found with ID: UNKNOWN",
  "path": "/orders/UNKNOWN"
}
```

Examples of handled failures include:

- Invalid request data
- Unknown order IDs
- Malformed JSON
- Constraint violations
- Invalid arguments
- Duplicate active order IDs (`409 Conflict`)
- Unexpected server errors

---

## Swagger / OpenAPI

After starting the application, interactive API documentation is available at:

```text
http://localhost:8080/swagger-ui.html
```

The generated OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger can be used to create, retrieve, modify, and cancel orders directly from the browser.

---

## Database Configuration

TradeMatch uses PostgreSQL during normal application execution.

Default configuration:

```text
Database: tradematchexchange
Host: localhost
Port: 5432
Username: postgres
```

The password is intentionally not stored in the repository.

Set it through an environment variable.

### Git Bash

```bash
export DB_PASSWORD=your_password
```

Optional datasource overrides:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/tradematchexchange
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

This keeps database credentials outside source control.

---

## Running the Application

### Prerequisites

- Java 17 or newer
- Maven
- PostgreSQL
- Git

### Clone

```bash
git clone https://github.com/Rajkumar0863/TradeMatchExchange.git
cd TradeMatchExchange
```

### Configure PostgreSQL

Create a database named:

```text
tradematchexchange
```

Then set the password:

```bash
export DB_PASSWORD=your_password
```

### Run Tests

```bash
mvn clean test
```

Expected result:

```text
Tests run: 70
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

### Start the Application

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080/swagger-ui.html
```

---

## Persistence and Startup Recovery

The exchange uses an in-memory order book for matching, but active orders are also persisted.

At application startup:

```text
PostgreSQL
    |
    v
Load active orders
    |
    v
Restore original timestamps
    |
    v
Rebuild in-memory order books
    |
    v
Exchange ready for matching
```

Preserving timestamps is important because recreating orders with new timestamps after a restart would silently change time priority.

---

## Testing Strategy

The project tests behaviour at multiple layers.

```text
Matching Engine Tests
        |
Order Book Tests
        |
Service Tests
        |
Repository Tests
        |
Controller Tests
        |
REST Integration Tests
```

Important scenarios covered include:

- Price-time priority
- Full fills
- Partial fills
- Market-order sweeping
- IOC behaviour
- FOK behaviour
- Order modification and reprioritisation
- Persistence
- Startup recovery
- REST request validation
- HTTP 400 responses
- HTTP 404 responses
- HTTP 409 duplicate-order conflicts
- HTTP 500 handling
- Create / read / update / delete API flows

Integration tests use an isolated in-memory H2 database, so the automated Maven test suite does not require local PostgreSQL credentials.

---

## Continuous Integration

GitHub Actions runs the Maven test suite automatically for repository changes.

The CI pipeline uses Java 17 and executes:

```bash
mvn --batch-mode clean test
```

This provides an automated regression check for the matching engine, persistence layer, and REST API.

---

## Project Structure

```text
src/
├── main/
│   ├── java/com/rajkumar/tradematchexchange/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── engine/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── service/
│   │   └── TradeMatchExchangeApplication.java
│   │
│   └── resources/
│       └── application.properties
│
└── test/
    └── java/com/rajkumar/tradematchexchange/
        ├── controller/
        ├── engine/
        ├── integration/
        ├── repository/
        └── service/
```

---

## Engineering Decisions

### PriorityQueue for Order Books

Java `PriorityQueue` provides efficient access to the highest-priority order while allowing custom ordering based on execution type, price, and timestamp.

### Separate Domain and Persistence Responsibilities

Matching behaviour is handled by the exchange domain layer while repositories provide durable storage. This keeps database concerns out of the core matching algorithm.

### Timestamp Preservation

Order timestamps are persisted and restored so application restarts do not change price-time priority.

### Modified Orders Lose Time Priority

An order whose quantity or price is changed receives a new timestamp and is reinserted into the queue. The modified timestamp is also persisted, preventing runtime and restart behaviour from diverging.

### Isolated Integration Tests

Integration tests use H2 rather than depending on a developer's PostgreSQL instance. This makes the test suite reproducible locally and in CI.

---

## Current Scope

TradeMatch is an exchange simulator and backend engineering project, not a production financial exchange.

The current implementation demonstrates:

- Order-book data structures
- Matching algorithms
- Transactional service logic
- Persistence and recovery
- REST API design
- Automated testing
- Continuous integration

Production exchange infrastructure would additionally require areas such as distributed coordination, high-availability architecture, durable event processing, authentication and authorization, observability, concurrency controls, and significantly more extensive performance and failure testing.

---

## Author

**Rajkumar Vijayan**

MSc Software Development (International Systems)  
University of Limerick

GitHub: `Rajkumar0863`