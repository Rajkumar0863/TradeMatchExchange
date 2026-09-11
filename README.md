# TradeMatch Exchange

A backend electronic trading exchange simulator built with Java and Spring Boot that models the core behaviour of an order-matching system.

TradeMatch maintains in-memory order books using price-time priority, supports multiple execution instructions, performs full and partial fills, persists active orders and executed trades to PostgreSQL, restores active exchange state after application restart, and exposes order-management functionality through a REST API.

The project focuses on matching correctness, state consistency, persistence, API design, automated testing, and continuous integration rather than UI development.

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
| `IOC` | Executes immediately for available quantity and cancels any remaining quantity |
| `FOK` | Executes only when the entire requested quantity can be filled immediately at compatible prices |

### Order Management

- Place orders
- Retrieve all active orders
- Retrieve individual orders by ID
- Modify order quantity and price
- Cancel active orders
- Maintain price-time priority correctly after modification
- Reject invalid order requests
- Reject duplicate active order IDs with HTTP `409 Conflict`

### Persistence and Recovery

- PostgreSQL persistence using Spring Data JPA
- Active order persistence
- Executed trade persistence
- Startup recovery of active orders
- Original timestamps preserved during recovery
- Modified timestamps persisted consistently
- Runtime and persisted order state synchronized after updates and executions

### REST API

- Spring Boot REST API
- Request validation
- Structured error responses
- Appropriate HTTP status codes
- Swagger / OpenAPI documentation
- Importable Postman collection

### Testing and CI

- Matching-engine unit tests
- Order-book tests
- Service tests
- Controller tests
- Repository tests
- REST integration tests
- H2-backed isolated integration testing
- Maven automated build
- GitHub Actions continuous integration

The current automated test suite contains **70 tests** covering the matching engine, order-book behaviour, persistence, service logic, API behaviour, validation, error handling, duplicate-order handling, and integration flows.

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

The REST layer is separated from the exchange domain logic. The matching engine operates on in-memory order books for efficient access, while PostgreSQL provides durable storage for active orders and executed trades.

For a more detailed discussion of the architecture and design decisions, see:

```text
docs/ARCHITECTURE.md
```

---

## Price-Time Priority

TradeMatch uses **price-time priority**, a common ordering rule in electronic markets.

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

When an order's price or quantity is modified, TradeMatch assigns it a new timestamp and reinserts it into the priority queue. The modified order therefore loses its previous time priority.

The new timestamp is also persisted so runtime behaviour and restart behaviour remain consistent.

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

TradeMatch also handles partial fills by reducing the remaining quantity of an order instead of incorrectly removing the entire order.

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

Example request:

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

Example successful response:

```json
{
  "status": "SUCCESS",
  "orderId": "BUY-1001",
  "message": "Order placed successfully."
}
```

---

### Get All Active Orders

```http
GET /orders
```

---

### Get an Order by ID

```http
GET /orders/{orderId}
```

Example:

```http
GET /orders/BUY-1001
```

---

### Modify an Order

```http
PUT /orders/{orderId}
Content-Type: application/json
```

Example request:

```json
{
  "quantity": 150,
  "price": 205.0
}
```

Changing the quantity or price causes the order to receive a new timestamp and therefore lose its previous time priority.

---

### Cancel an Order

```http
DELETE /orders/{orderId}
```

Example:

```http
DELETE /orders/BUY-1001
```

---

## HTTP Error Handling

The API returns structured HTTP errors rather than exposing internal exceptions.

Example `404 Not Found` response:

```json
{
  "timestamp": "2026-09-10T00:00:00",
  "status": 404,
  "error": "Order Not Found",
  "message": "Order not found with ID: UNKNOWN",
  "path": "/orders/UNKNOWN"
}
```

Handled API failures include:

| Scenario | HTTP Status |
|---|---:|
| Invalid request data | `400 Bad Request` |
| Malformed JSON | `400 Bad Request` |
| Constraint violation | `400 Bad Request` |
| Invalid argument | `400 Bad Request` |
| Unknown order | `404 Not Found` |
| Duplicate active order ID | `409 Conflict` |
| Unexpected server error | `500 Internal Server Error` |

Duplicate active order IDs are rejected before normal order placement proceeds, preventing an existing active order from being silently replaced through the standard API flow.

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

## Postman Collection

An importable Postman collection is included at:

```text
postman/TradeMatchExchange.postman_collection.json
```

The collection includes requests for:

- Creating an order
- Retrieving all active orders
- Retrieving an order by ID
- Updating an order
- Cancelling an order
- Testing duplicate-order `409 Conflict` handling

The collection uses:

```text
http://localhost:8080
```

as the default `baseUrl`.

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

The database password is intentionally **not stored in the repository**.

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

### Clone the Repository

```bash
git clone https://github.com/Rajkumar0863/TradeMatchExchange.git
cd TradeMatchExchange
```

### Configure PostgreSQL

Create a PostgreSQL database named:

```text
tradematchexchange
```

Then set the database password:

```bash
export DB_PASSWORD=your_password
```

### Run the Automated Tests

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

TradeMatch uses in-memory order books for matching while active orders are also persisted in PostgreSQL.

At application startup:

```text
PostgreSQL
    |
    v
Load active orders
    |
    v
Restore persisted timestamps
    |
    v
Rebuild in-memory order books
    |
    v
Exchange ready for matching
```

Preserving timestamps is important because recreating active orders with new timestamps after a restart would silently change their time priority.

This allows the exchange to reconstruct its active order books without changing the ordering semantics of previously submitted orders.

---

## Order Modification and Priority

Price-time priority requires modifications to be handled carefully.

When an active order is modified:

```text
Remove order from PriorityQueue
            |
            v
Update quantity / price
            |
            v
Assign new timestamp
            |
            v
Reinsert into PriorityQueue
            |
            v
Persist modified state
```

This ensures a modified order does not incorrectly retain its previous position ahead of newer orders at the same price.

The same timestamp is retained in persistence so restarting the application does not produce different ordering behaviour.

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
- Duplicate active order rejection
- REST request validation
- HTTP `400` responses
- HTTP `404` responses
- HTTP `409` duplicate-order conflicts
- HTTP `500` handling
- Create / read / update / delete API flows

Integration tests use an isolated in-memory H2 database, so the automated Maven test suite does not require local PostgreSQL credentials.

### Current Test Suite

```text
Tests run: 70
Failures: 0
Errors: 0
Skipped: 0
```

---

## Continuous Integration

GitHub Actions automatically builds and tests the project for changes to the configured branches.

The CI environment uses Java 17 and executes:

```bash
mvn --batch-mode clean test
```

This provides an automated regression check for the matching engine, persistence layer, service layer, and REST API.

The current `main` branch passes the complete automated test suite.

---

## Project Structure

```text
TradeMatchExchange/
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── docs/
│   └── ARCHITECTURE.md
│
├── postman/
│   └── TradeMatchExchange.postman_collection.json
│
├── src/
│   ├── main/
│   │   ├── java/com/rajkumar/tradematchexchange/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── engine/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── TradeMatchExchangeApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/rajkumar/tradematchexchange/
│           ├── controller/
│           ├── engine/
│           ├── integration/
│           ├── repository/
│           └── service/
│
├── .gitignore
├── LICENSE
├── pom.xml
└── README.md
```

---

## Engineering Decisions

### PriorityQueue-Based Order Books

Java `PriorityQueue` is used to maintain BUY and SELL order priority.

The queue comparators implement the exchange ordering rules:

```text
BUY  -> higher price first -> earlier timestamp first
SELL -> lower price first  -> earlier timestamp first
```

This keeps matching behaviour explicit in the domain layer.

### Separate Domain and Persistence Responsibilities

Matching behaviour is handled by the exchange domain layer while Spring Data repositories provide durable storage.

This keeps database concerns separate from the core matching algorithm.

### Timestamp Preservation

Order timestamps are persisted and restored so application restarts do not silently change price-time priority.

### Modified Orders Lose Time Priority

An order whose quantity or price changes receives a new timestamp and is reinserted into the queue.

The modified timestamp is also persisted, preventing runtime and restart behaviour from diverging.

### Isolated Integration Tests

Integration tests use H2 rather than depending on a developer's PostgreSQL instance.

This makes the automated test suite reproducible locally and in GitHub Actions.

### Explicit API Failure Semantics

The REST layer maps application failures to meaningful HTTP responses rather than returning successful responses for failed operations.

Examples include:

```text
Invalid request          -> 400 Bad Request
Unknown order            -> 404 Not Found
Duplicate active order   -> 409 Conflict
Unexpected exception     -> 500 Internal Server Error
```

---

## Consistency Model

TradeMatch combines:

```text
In-memory order books
        +
PostgreSQL persistence
```

The current implementation keeps these states synchronized during normal order placement, modification, cancellation, matching, and startup recovery.

However, the in-memory exchange and relational database do **not** form a single atomic transactional system.

For example, a database transaction rollback cannot automatically reverse an in-memory order-book mutation.

This is an intentional scope boundary of the current simulator.

A more advanced exchange architecture could address this using approaches such as durable command logs, event sourcing, transactional outbox patterns, replayable events, or a single-writer matching architecture.

---

## Current Scope

TradeMatch is an **exchange simulator and backend engineering project**, not a production financial exchange.

The current implementation demonstrates:

- Order-book data structures
- Matching algorithms
- Price-time priority
- Multiple execution instructions
- REST API design
- Persistence and startup recovery
- State-consistency considerations
- Structured HTTP error handling
- Automated testing
- Continuous integration

Production exchange infrastructure would additionally require areas such as distributed coordination, high-availability architecture, durable event processing, authentication and authorization, observability, stronger concurrency controls, decimal-safe financial price representation, and significantly more extensive performance and failure testing.

---

## Future Improvements

Potential extensions include:

- `BigDecimal`-based price representation
- Trade-history REST endpoints
- Market-data endpoints
- Order-book depth APIs
- Authentication and authorization
- Metrics and observability
- Containerized deployment
- Improved concurrency control
- Event-driven persistence
- Performance and load benchmarking

These are intentionally outside the current project scope.

---

## Author

**Rajkumar Vijayan**

MSc Software Development (International Systems)  
University of Limerick

GitHub: `Rajkumar0863`

---

## License

This project is licensed under the MIT License.
