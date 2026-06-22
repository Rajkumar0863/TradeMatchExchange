# TradeMatchExchange

A Java-based stock exchange matching engine that simulates how modern electronic exchanges process and execute trades.

The system implements heap-based order books, price-time priority matching, partial order fills, trade history tracking, and market analytics.

---

## Features

### Order Management

* Buy Order Book (Max Heap)
* Sell Order Book (Min Heap)
* Price-Time Priority Matching
* Limit Orders
* Partial Order Fills

### Trade Execution

* Automatic Trade Matching
* Trade History Tracking
* Execution Price Calculation
* Trade Quantity Management

### Market Analytics

* Total Trades
* Total Volume
* Highest Trade Price
* Lowest Trade Price
* Average Trade Price

---

## System Architecture

```text
BUY ORDERS (Max Heap)
          |
          v
     Order Book
          |
          v
   Matching Engine
          |
          v
SELL ORDERS (Min Heap)

          |
          v

     Trade History
          |
          v

   Market Statistics
```

---

## Technologies Used

* Java
* Object-Oriented Programming (OOP)
* PriorityQueue
* Heap Data Structures
* Collections Framework
* LocalDateTime API

---

## Core Concepts Implemented

### Data Structures

* Priority Queue
* Max Heap
* Min Heap
* ArrayList

### Algorithms

* Price-Time Priority Matching
* Partial Fill Processing
* Trade Execution Logic

### OOP Concepts

* Classes and Objects
* Encapsulation
* Constructors
* Enums
* Method Overriding

---

## Sample Output

```text
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

## Project Structure

```text
src
│
├── engine
│   └── OrderBook.java
│
├── model
│   ├── Order.java
│   ├── Trade.java
│   ├── OrderType.java
│   ├── OrderExecutionType.java
│   └── MarketStatistics.java
│
└── Main.java
```

---

## Future Enhancements

* VWAP (Volume Weighted Average Price)
* Total Traded Value
* Best Bid / Best Ask
* JUnit Testing
* Spring Boot REST API
* PostgreSQL Integration
* Docker Deployment

---

## Author

Rajkumar Vijayan

MSc Software Development (International Systems)
University of Limerick

GitHub: Rajkumar0863
LinkedIn: Rajkumar Vijayan
