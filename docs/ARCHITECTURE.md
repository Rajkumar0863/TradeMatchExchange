# TradeMatch Exchange — Architecture

## Overview

TradeMatch Exchange is a Java and Spring Boot backend that simulates the core behaviour of an electronic securities exchange.

The system combines:

- in-memory price-time-priority order books
- a matching engine
- REST API endpoints
- PostgreSQL persistence
- startup recovery
- automated tests
- CI through GitHub Actions

The architecture separates exchange-domain behaviour from persistence and HTTP concerns so matching logic remains testable independently of the web and database layers.

---

## High-Level Architecture

```text
                    +----------------------+
                    |      REST Client     |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |   OrderController    |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |     OrderService     |
                    +----------+-----------+
                       |              |
                       |              |
                       v              v
              +----------------+   +------------------+
              |    Exchange    |   | OrderRepository  |
              +-------+--------+   +---------+--------+
                      |                      |
                      v                      |
              +----------------+             |
              |   OrderBook    |             |
              +-------+--------+             |
                      |                      |
                      v                      |
              +----------------+             |
              | MatchingEngine |             |
              +-------+--------+             |
                      |                      |
                      v                      |
              +----------------+             |
              |     Trade      |             |
              +-------+--------+             |
                      |                      |
                      v                      v
                  +------------------------------+
                  |          PostgreSQL          |
                  |     Orders + Trade History   |
                  +------------------------------+
```

---

## Main Components

## 1. OrderController

`OrderController` is the HTTP-facing layer.

Responsibilities:

- receive REST requests
- validate incoming request bodies
- delegate business operations to `OrderService`
- return API responses
- expose create, read, update, and delete operations

It should not contain exchange-matching logic.

---

## 2. OrderService

`OrderService` coordinates application-level behaviour.

Responsibilities include:

- creating domain `Order` objects from request DTOs
- persisting active orders
- invoking the exchange
- triggering matching
- synchronising changed order state back to persistence
- retrieving active orders
- updating orders
- deleting cancelled or fully filled orders

The service acts as the boundary between REST/persistence concerns and exchange-domain behaviour.

---

## 3. Exchange

`Exchange` manages the active in-memory exchange state.

It maintains one `OrderBook` for each registered stock symbol.

Responsibilities:

- create stock order books
- accept validated orders
- restore persisted active orders after restart
- invoke the matching engine
- cancel orders
- modify orders
- track active order IDs through the risk layer

Simplified structure:

```text
Exchange
 |
 +-- AAPL -> OrderBook
 |
 +-- MSFT -> OrderBook
 |
 +-- other symbols -> OrderBook
```

---

## 4. OrderBook

Each stock has an independent `OrderBook`.

An order book contains two priority queues:

```text
BUY side                    SELL side

PriorityQueue               PriorityQueue
     |                           |
     v                           v
Higher price first          Lower price first
Earlier time first          Earlier time first
```

### BUY Comparator

BUY orders are prioritised by:

1. execution characteristics
2. higher price
3. earlier timestamp

### SELL Comparator

SELL orders are prioritised by:

1. execution characteristics
2. lower price
3. earlier timestamp

This provides price-time priority.

---

## 5. MatchingEngine

`MatchingEngine` is responsible for matching compatible BUY and SELL orders.

Its responsibilities include:

- determining whether orders can execute
- calculating execution quantity
- calculating execution price
- updating remaining quantities
- producing trade records
- sweeping multiple price levels when required
- removing completed or immediate-order remainders appropriately

The matching engine operates directly on the in-memory order book.

---

## Matching Rules

## LIMIT

A LIMIT order executes only when an acceptable opposing price exists.

Example:

```text
BUY LIMIT 100 @ 200
SELL LIMIT 100 @ 195
```

The orders are compatible because:

```text
BUY price >= SELL price
```

The remaining quantity may stay on the order book if the order is only partially filled.

---

## MARKET

A MARKET order attempts immediate execution against available opposing liquidity.

Example SELL book:

```text
30 @ 190
40 @ 195
50 @ 205
```

Incoming order:

```text
MARKET BUY 70
```

Execution:

```text
30 @ 190
40 @ 195
```

This demonstrates multi-level liquidity sweeping.

---

## IOC

IOC means Immediate-Or-Cancel.

The system:

1. executes immediately against compatible liquidity
2. allows a partial fill
3. removes any remaining unfilled quantity

Example:

```text
IOC BUY 100
Available SELL liquidity = 60
```

Result:

```text
60 executed
40 cancelled
```

---

## FOK

FOK means Fill-Or-Kill.

Before execution, the engine verifies that the entire requested quantity can be filled at compatible prices.

If sufficient liquidity does not exist:

```text
No partial execution occurs
```

If sufficient liquidity exists:

```text
The full order executes immediately
```

This prevents accidental partial fills of FOK orders.

---

## Partial Fill Handling

Suppose:

```text
BUY 100 @ 200
SELL 40 @ 195
```

Trade:

```text
Quantity = 40
```

Remaining BUY order:

```text
BUY 60 @ 200
```

The remaining active order stays in the order book and its updated quantity is persisted.

---

## Order Modification

Changing an active order requires special care because a `PriorityQueue` depends on values used by its comparator.

TradeMatch therefore follows this sequence:

```text
Locate order
    |
    v
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
```

A modified order loses its original time priority.

The same modified order state, including the new timestamp, is persisted to PostgreSQL.

This prevents runtime behaviour from differing from behaviour after an application restart.

---

## Persistence Model

TradeMatch uses PostgreSQL as durable storage.

Main persisted data:

```text
orders
trades
```

### Active Orders

The orders table represents currently active persisted orders.

Orders are updated after partial execution and removed when they become inactive.

### Trades

Completed executions are written to trade history.

Trade identifiers use UUID-based IDs.

Example:

```text
TRD-14B75D0A-D8C1-479A-8E60-BB8759024A34
```

---

## Startup Recovery

The matching engine itself is in memory.

Without recovery, restarting the application would lose the active order books even though active orders still existed in PostgreSQL.

TradeMatch rebuilds exchange state during startup.

```text
Application starts
       |
       v
Read active orders from PostgreSQL
       |
       v
Register stock order books
       |
       v
Restore each persisted Order object
       |
       v
Preserve persisted timestamps
       |
       v
In-memory exchange reconstructed
```

Restored orders are not treated as new orders.

They are not re-persisted and are not automatically re-matched during restoration.

---

## Why Timestamp Preservation Matters

Consider two equal-price orders:

```text
ORDER-A  @ 200  10:00
ORDER-B  @ 200  10:05
```

Correct priority:

```text
ORDER-A
ORDER-B
```

If recovery recreated `ORDER-A` with the current time, the order could incorrectly move behind `ORDER-B`.

Persisting and restoring the original timestamp prevents this.

---

## RiskManager

The project contains a lightweight risk-management layer.

Its current responsibilities include validating incoming orders and tracking active order IDs.

It is intentionally limited in scope.

A real exchange risk engine could additionally enforce:

- position limits
- credit limits
- maximum notional exposure
- fat-finger controls
- account-level restrictions

These are outside the current project scope.

---

## REST API Architecture

```text
HTTP
 |
 v
OrderController
 |
 v
DTO validation
 |
 v
OrderService
 |
 +------> OrderRepository
 |
 v
Exchange
 |
 v
OrderBook / MatchingEngine
```

The controller does not access the matching engine directly.

This maintains separation between transport, orchestration, persistence, and domain behaviour.

---

## Error Handling

A global exception handler translates failures into structured HTTP responses.

Examples:

```text
Validation failure       -> 400 Bad Request
Malformed JSON           -> 400 Bad Request
Invalid argument         -> 400 Bad Request
Unknown order            -> 404 Not Found
Duplicate active order   -> 409 Conflict
Unexpected exception     -> 500 Internal Server Error
```

The API returns structured error bodies rather than exposing stack traces.

---

## Testing Architecture

The project contains multiple test layers.

```text
Unit tests
    |
    +-- MatchingEngine
    +-- OrderBook
    +-- service behaviour

Repository tests
    |
    +-- JPA persistence

Controller tests
    |
    +-- HTTP mapping
    +-- validation
    +-- status codes

Integration tests
    |
    +-- Spring context
    +-- REST endpoints
    +-- persistence
    +-- exchange state
```

Current regression suite:

```text
70 tests
0 failures
0 errors
```

---

## Test Database Strategy

Production runtime uses PostgreSQL.

Automated integration tests use an isolated H2 database.

```text
Runtime
    -> PostgreSQL

Tests
    -> H2 in-memory database
```

This means:

- CI does not require a PostgreSQL service
- developer database credentials are not required for tests
- tests start with isolated state
- tests do not depend on execution order

---

## Continuous Integration

GitHub Actions executes the test suite using Java 17.

Pipeline:

```text
Push / Pull Request
        |
        v
Checkout repository
        |
        v
Install Java 17
        |
        v
Maven clean test
        |
        v
Build succeeds or fails
```

Current Maven command:

```bash
mvn --batch-mode clean test
```

---

## Consistency Model

TradeMatch intentionally combines:

```text
In-memory state
+
Persistent state
```

The in-memory order book is used for matching because priority queues are suitable for fast access to the best available order.

PostgreSQL provides durability.

The service layer synchronises the two representations when orders:

- enter the system
- partially execute
- complete
- are modified
- are cancelled

---

## Known Architectural Limitation

The in-memory exchange and PostgreSQL do not participate in one distributed atomic transaction.

For example, an in-memory modification may occur before a later database failure.

A production trading system would require stronger consistency mechanisms, such as:

- event sourcing
- durable command logs
- transactional outbox patterns
- replayable state
- single-writer processing
- stronger failure recovery

This project intentionally stops short of implementing distributed exchange infrastructure.

---

## Package Structure

```text
com.rajkumar.tradematchexchange
|
+-- controller
|     REST controllers
|
+-- dto
|     API request / response objects
|
+-- engine
|     OrderBook and matching logic
|
+-- exception
|     API/domain exception handling
|
+-- model
|     Order, Trade, enums, statistics
|
+-- repository
|     Spring Data JPA repositories
|
+-- risk
|     Basic order validation / tracking
|
+-- service
|     Exchange and application services
|
+-- TradeMatchExchangeApplication
      Spring Boot entry point
```

---

## Design Goals

TradeMatch was designed around the following goals:

1. correct price-time-priority behaviour
2. clear separation of responsibilities
3. deterministic order handling
4. persistence of active state
5. restart recovery
6. testable business logic
7. REST accessibility
8. reproducible automated testing
9. CI-backed regression protection

---

## Non-Goals

TradeMatch is not intended to replicate the infrastructure of a real securities exchange.

The current project does not claim to provide:

- regulatory-grade trading infrastructure
- multi-node high availability
- distributed consensus
- ultra-low-latency networking
- FIX connectivity
- authentication or brokerage accounts
- production market data feeds
- real financial transactions

Its purpose is to demonstrate backend software engineering and exchange-domain concepts through a functional simulator.