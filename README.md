# TradeMatchExchange

A Java stock exchange matching engine that simulates how electronic exchanges accept, validate, match and settle orders.

The engine maintains a heap-based order book per stock symbol, matches orders using price-time priority, supports partial fills and multiple execution types, validates orders before they reach the book, and records executed trades with market statistics and CSV export.

---

## Features

### Order Management
- Buy order book (max heap) and sell order book (min heap), backed by `PriorityQueue`
- Price-time priority matching
- Execution types: `LIMIT`, `MARKET`, `IOC` (Immediate-Or-Cancel), `FOK` (Fill-Or-Kill)
- Partial order fills
- Order cancellation and modification
- Best bid / best ask lookup and market depth display

### Risk Control
- Pre-trade validation before an order enters the book
- Rejects null orders, missing IDs and symbols, and duplicate order IDs
- Enforces maximum order quantity and maximum price bounds
- Price checks are skipped for `MARKET` orders, which execute at the best available price

### Trade Execution and Reporting
- Automatic matching with trade generation and sequential trade IDs
- In-memory trade repository (`TradeRepository`)
- CSV export of executed trades (`TradeExporter`)
- Market statistics: total trades, total volume, total traded value, highest, lowest and average trade price, and **VWAP**

### Multi-Stock Support
- `Exchange` manages an independent order book per stock symbol, with order placement, matching, cancellation, modification and display routed through a single service class

---

## System Architecture

```
            Order
              |
              v
        RiskManager            (pre-trade validation)
              |
              v
         OrderBook             (BUY: max heap / SELL: min heap)
              |
              v
       MatchingEngine          (price-time priority, partial fills,
              |                 LIMIT / MARKET / IOC / FOK)
              v
      TradeRepository          (trade history)
              |
      +-------+--------+
      |                |
      v                v
MarketStatistics  TradeExporter
 (incl. VWAP)        (CSV)
```

---

## Technologies Used

- Java 17
- Object-Oriented Programming
- `PriorityQueue`, heap data structures, Collections Framework
- `LocalDateTime` API
- JUnit 5 (Jupiter)

---

## Project Structure

```
src
├── Main.java                        Demo driver
├── OrderTest.java                   Manual test harness
│
├── model
│   ├── Order.java
│   ├── Trade.java
│   ├── OrderType.java               BUY / SELL
│   ├── OrderExecutionType.java      LIMIT / MARKET / IOC / FOK
│   └── MarketStatistics.java        Volume, high/low/avg, VWAP
│
├── engine
│   ├── OrderBook.java               Heap-based book, cancel/modify, best bid/ask
│   ├── MatchingEngine.java          Core matching loop
│   └── MarketDepth.java
│
├── risk
│   └── RiskManager.java             Pre-trade validation
│
├── repository
│   └── TradeRepository.java         Trade history
│
├── service
│   └── Exchange.java                Multi-stock facade
│
├── utils
│   └── TradeExporter.java           CSV export
│
└── test
    ├── OrderJUnitTest.java
    ├── OrderBookJUnitTest.java
    ├── OrderCancellationJUnitTest.java
    ├── OrderModificationJUnitTest.java
    └── OrderBookTest.java           Manual test harness
```

---

## How to Run

### Option 1 — Maven (recommended)

The project uses the standard Maven layout (`src/main/java`, `src/test/java`).

```bash
# Compile
mvn compile

# Run the demo
mvn exec:java -Dexec.mainClass="Main"

# Run the test suite
mvn test
```

### Option 2 — Plain javac

```bash
# Compile the application sources
javac -d out $(find src/main/java -name "*.java")

# Run the demo
java -cp out Main
```

Running the tests without Maven requires the JUnit 5 console launcher on the classpath.

---

## Testing

The test suite uses JUnit 5 and covers order construction and validation, order book insertion and ordering, cancellation, and modification.

```bash
mvn test
```

---

## Sample Output

```
TRADE EXECUTED
BUY : ORD001
SELL: ORD003
Trade Quantity: 40
Execution Price: 190.0

===== MARKET STATISTICS =====

Total Trades: 4
Total Volume: 160
Highest Price: 210.0
Lowest Price: 190.0
Average Price: 201.25
```

---

## Roadmap

- Stop-loss and iceberg order types
- Order book snapshots and replay
- Spring Boot REST API over the `Exchange` service
- PostgreSQL persistence for trade history
- Latency benchmarking of the matching loop
- Docker deployment

---

## Author

**Rajkumar Vijayan**
MSc Software Development (International Systems), University of Limerick

- GitHub: [Rajkumar0863](https://github.com/Rajkumar0863)
- LinkedIn: [rajkumar-vijayan](https://www.linkedin.com/in/rajkumar-vijayan-0135a8338/)

---

## License

MIT
